package com.github.MT_0.permutations;

import java.util.ConcurrentModificationException;
import java.util.Iterator;

/**
 * <p>A doubly linked list structure that allows elements of the given enumerated type
 * to be added to, or retrieved from, the head or tail of the list and can iterate over
 * the entire list. The structure has no capability to: remove items from the list; edit
 * the order of the list; insert items in the middle of the list; or directly access
 * elements in the middle of the list.</p>
 * 
 * <p>The structure can be accessed in Constant time to:</p>
 * <ul>
 * <li>Add an element to the head or tail;</li>
 * <li>Concatenate this list with another of the same enumerated type;</li>
 * <li>Retrieve the head or tail element of the list; and</li>
 * <li>Get the next element in the list from the iterator.</li>
 * </ul>
 * <p>The structure can be accessed in Linear tile to:</p>
 * <ul>
 * <li>Query the list to see if it contains an element; and</li>
 * <li>Get a string representation of the list.</li>
 * </ul>
 * @author Martyn Taylor
 *
 * @param <E>
 */
public class UnmodifiableLinkList<E> implements Iterable<E> {
	/**
	 * Whether elements can be removed from the list.
	 */
	protected boolean isModifiable = false;

	/**
	 * The wrapper for the head element of the list.
	 */
	protected ListElement head = null;

	/**
	 * The wrapper for the tail element of the list.
	 */
	protected ListElement tail = null;

	/**
	 * The number of elements in the list.
	 */
	protected int size = 0;

	/**
	 * A reference variable to track changes to the data structure and
	 * is used to highlight concurrent modification exceptions.
	 */
	protected long changes = Long.MIN_VALUE;

	/**
	 * Checks whether elements can be removed from the list.
	 * @return boolean
	 */
	public boolean canRemoveElements() {
		return isModifiable;
	}

	/**
	 * Checks whether there are no elements in the list.
	 * @return boolean
	 */
	public boolean isEmpty() {
		return size() == 0;
	}

	/**
	 * Add an element to the head of the list.
	 * 
	 * @param element - The non-null element to be added.
	 * @throws ConcurrentModificationException Thrown if the list is modified
	 * 			during the execution of this method. 
	 * @throws IllegalArgumentException Thrown if the head of the list is connected
	 * 			to another list and cannot be appended to.
	 */
	public void addHead( E element ) {
		if ( element == null )
			throw new IllegalArgumentException( "Cannot add a null element." );
		if ( head != null && head.getHeadwards() != null )
			throw new IllegalArgumentException( "Head element is connected to another list and cannot be appended to.");
		long change = changes;
		if ( size == 0 ) {
			head = tail = new ListElement( element );
		} else {
			ListElement le = new ListElement( element );
			le.setTailwards( head );
			head.setHeadwards( le );
			head = le;
		}
		size++;
		if ( change != changes )
			throw new ConcurrentModificationException();
		incrementChange();
	}

	/**
	 * Add an element to the tail of the list.
	 * 
	 * @param element - The non-null element to be added.
	 * @throws ConcurrentModificationException Thrown if the list is modified
	 * 			during the execution of this method.
	 * @throws IllegalArgumentException Thrown if the tail of the list is connected
	 * 			to another list and cannot be appended to.
	 */
	public void addTail( E element ) {
		if ( element == null )
			throw new IllegalArgumentException( "Cannot add a null element." );
		if ( tail != null && tail.getTailwards() != null )
			throw new IllegalArgumentException( "Tail element is connected to another list and cannot be appended to.");
		long change = changes;
		if ( size == 0 ) {
			head = tail = new ListElement( element );
		} else {
			ListElement le = new ListElement( element );
			le.setHeadwards( tail );
			tail.setTailwards( le );
			tail = le;
		}
		size++;
		if ( change != changes )
			throw new ConcurrentModificationException();
		incrementChange();
	}

	/**
	 * Returns the head element from the list or null if the list is empty.
	 * 
	 * @return &lt;E&gt;
	 */
	public E getHead() {
		if ( size == 0 )
			return null;
		return head.getElement();
	}

	/**
	 * Returns the tail element from the list or null if the list is empty.
	 * 
	 * @return &lt;E&gt;
	 */
	public E getTail() {
		if ( size == 0 )
			return null;
		return tail.getElement();
	}

	/**
	 * Returns the element before the tail element from the list or null if
	 * the list does not have at least two elements.
	 * 
	 * @return &lt;E&gt;
	 */
	public E getPenultimateTail() {
		if ( size < 2 )
			return null;
		return tail.getHeadwards().getElement();
	}

	/**
	 * Returns the size of the list.
	 * @return int
	 */
	public int size() {
		return size;
	}

	/**
	 * Appends the contents of the given list to the head of this list.
	 * 
	 * @param list - A non-null list of the same enumerated type.
	 * @throws IllegalArgumentException Thrown if the head of this list or the tail
	 * 			of the given list are connected to another list and, therefore, cannot
	 * 			be appended to.
	 */
	public void addAllHead( UnmodifiableLinkList<E> list ) {
		if ( list == null )
			throw new IllegalArgumentException( "Cannot append a null list." );
		if ( head != null && head.getHeadwards() != null )
			throw new IllegalArgumentException( "This list's head element is connected to another list and cannot be appended to.");
		if ( list.tail != null && list.tail.getTailwards() != null )
			throw new IllegalArgumentException( "Given list's tail element is connected to another list and cannot be appended to.");
		if ( list.size() == 0 )
			return;
		long change = changes;
		long listChange = list.changes;

		isModifiable &= list.isModifiable;

		if ( size == 0 ) {
			head = list.head;
			tail = list.tail;
			size = list.size;
		} else {
			head.setHeadwards( list.tail );
			list.tail.setTailwards( head );
			head = list.head;
			size += list.size;
		}

		list.head = list.tail = null;
		list.size = 0;

		if ( change != changes || listChange != list.changes )
			throw new ConcurrentModificationException();
		incrementChange();
	}

	/**
	 * Appends the contents of the given list to the tail of this list.
	 * 
	 * @param list - A non-null list of the same enumerated type.
	 * @throws IllegalArgumentException Thrown if the tail of this list or the head
	 * 			of the given list are connected to another list and, therefore, cannot
	 * 			be appended to.
	 */
	public void addAllTail( UnmodifiableLinkList<E> list ) {
		if ( list == null )
			throw new IllegalArgumentException( "Cannot append a null list." );
		if ( tail != null && tail.getTailwards() != null )
			throw new IllegalArgumentException( "This list's tail element is connected to another list and cannot be appended to.");
		if ( list.head != null && list.head.getHeadwards() != null )
			throw new IllegalArgumentException( "Given list's head element is connected to another list and cannot be appended to.");
		if ( list.size() == 0 )
			return;
		long change = changes;
		long listChange = list.changes;

		isModifiable &= list.isModifiable;

		if ( size == 0 ) {
			head = list.head;
			tail = list.tail;
			size = list.size;
		} else {
			tail.setTailwards( list.head );
			list.head.setHeadwards( tail );
			tail = list.tail;
			size += list.size;
		}

		list.head = list.tail = null;
		list.size = 0;

		if ( change != changes || listChange != list.changes )
			throw new ConcurrentModificationException();
		incrementChange();
	}

	/**
	 * Checks whether a given element is contained within the list.
	 * @param element - The element to locate in the list. 
	 * @return boolean
	 */
	public boolean contains( final E element ) {
		return findElement( element ) != null;
	}

	/**
	 * Finds the internal list data structure containing the given element
	 * within the list. 
	 * @param element - The element to locate in the list.
	 * @return ListElement
	 */
	protected ListElement findElement( final E element ) {
		ListElement current = head;
		while ( current != null ) {
			if ( current.getElement() == element )
				return current;
			current = current.getTailwards();
		}
		return null;
	}

	/**
	 * Returns a comma-space separated sequence representing the contents
	 * of the list. The return value is bracketed by angle brackets (<>). 
	 * @see UnmodifiableLinkList#toString(String) toString( String )
	 * @return String 
	 */
	public String toString() {
		return toString( ", " );
	}

	/**
	 * Returns a sequence representing the contents of the list. The return
	 * value is bracketed by angle brackets (<>) and pairs of elements are
	 * separated by the value of the function's separator argument. 
	 * @param separator - A string defining the separator between list
	 * 			elements.
	 * @return String
	 */
	public String toString( String separator ) {
		StringBuffer b = new StringBuffer();
		b.append( '<' );
		boolean first = true;
		for ( E e: this ) {
			if ( first )
				first = false;
			else
				b.append( separator );
			b.append( e );
		}
		b.append( '>' );
		return b.toString();
		
	}

	/**
	 * <p>Increments the counter recording the number of changes to the
	 * data structure.</p>
	 * <p>Used as a simple (imperfect) method to detect concurrent
	 * modification of a list.</p>
	 */
	protected synchronized void incrementChange() {
		if ( changes == Long.MAX_VALUE )
			changes = Long.MIN_VALUE;
		else
			changes++;
	}

	/**
	 * <p>The data structure used to store a single element of data within
	 * the list</p>
	 * @author Martyn G. Taylor
	 */
	protected class ListElement {
		private E element;
		private ListElement tailwards = null;
		private ListElement headwards = null;

		protected ListElement( E element ) {
			this.element = element;
		}

		protected void setElement( E element ) 			{ this.element = element; }
		protected void setTailwards( ListElement next ) { this.tailwards = next; }
		protected void setHeadwards( ListElement prev ) { this.headwards = prev; }

		protected E getElement()	{ return element; }
		protected ListElement getTailwards()	{ return tailwards; }
		protected ListElement getHeadwards()	{ return headwards; }
	}

	/**
	 * Returns an iterator to loop through the list from head-to-tail.
	 * @return Iterator<E>
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			ListElement current = head;

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public E next() {
				if ( current == null )
					return null;
				E next = current.getElement();
				current = current.getTailwards();
				return next;
			}

			@Override
			public void remove() {
				throw new IllegalArgumentException( "Cannot remove from this list." );
			}
		};
	}

	/**
	 * Returns an iterator to loop through the list from tail-to-head.
	 * @return Iterator<E>
	 */
	public Iterator<E> reverseIterator() {
		return new Iterator<E>() {
			ListElement current = tail;

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public E next() {
				if ( current == null )
					return null;
				E next = current.getElement();
				current = current.getHeadwards();
				return next;
			}

			@Override
			public void remove() {
				throw new IllegalArgumentException( "Cannot remove from this list." );
			}
		};
	}
}