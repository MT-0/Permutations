package mgtaylor.datastructures;

import java.util.Iterator;
import java.util.ConcurrentModificationException;

/**
 * <p>An extension of the UnmodifiableLinkList structure that adds
 * functionality to:</p>
 * <ul>
 * <li>Remove a given element from the list.</li>
 * <li>Remove the head element from the list.</li>
 * <li>Remove the tail element from the list.</li>
 * <li>Clear (remove all elements from) the list.</li>
 * <li>Remove the element at the current iterator position.</li>
 * </ul>
 * @author Martyn G. Taylor
 * @see mgtaylor.datastructures.UnmodifiableLinkList UnmodifiableLinkList
 * @param <E>
 */
public class LinkList<E> extends UnmodifiableLinkList<E>
			implements Iterable<E> {

	/**
	 * Constructor to set List to be modifiable.
	 */
	public LinkList() {
		super();
		this.isModifiable = true;
	}

	/**
	 * Removes a given list element from the list.
	 * @param le
	 * @return ListElementStore - A container used to store the
	 * 		current and next elements of the list to keep a valid
	 * 		state for an iterator.
	 */
	protected ListElementStore removeElement( ListElement le ) {
		if ( le == null )
			return null;
		
		if ( le == head && le == tail ) {
			head = tail = null;
			size = 0;
			return new ListElementStore( null, null );
		} else if ( le == head ) {
			head = head.getTailwards();
			if ( head != null )
				head.setHeadwards( null );
			le.setTailwards( null );
			size--;
			return new ListElementStore( null, head );
		} else if ( le == tail ) {
			tail = tail.getHeadwards();
			le.setHeadwards( null );
			if ( tail != null )
				tail.setTailwards( null );
			size--;
			return new ListElementStore( tail, null );
		} else {
			final ListElement current	= le.getHeadwards();
			final ListElement next		= le.getTailwards();
			current.setTailwards( next );
			next.setHeadwards( current );
			le.setHeadwards( null );
			le.setTailwards( null );
			size--;
			return new ListElementStore( current, next );
		}
	}

	/**
	 * Removes all elements from the list.
	 * @throws ConcurrentModificationException Thrown if the list is
	 *			modified during the execution of this method. 
	 * @throws IllegalArgumentException Thrown if the list cannot
	 *			be modified.
	 */
	public void clear() {
		if ( !isModifiable )
			throw new IllegalArgumentException(
					"mgtaylor.datastructures.LinkList.clear() - " +
					"List is not modifiable." );
		
		final long change = changes;

		head = tail = null;
		size = 0;
		isModifiable = true;

		if ( change != changes )
			throw new ConcurrentModificationException(
					"mgtaylor.datastructures.LinkList.clear()" );
		incrementChange();
	}

	/**
	 * Removes a given element from the list.
	 * @param element
	 * @return boolean - Whether the element was successfully
	 * 			found and removed. 
	 * @throws ConcurrentModificationException Thrown if the list is
	 *			modified during the execution of this method. 
	 * @throws IllegalArgumentException Thrown if the list cannot
	 *			be modified.
	 */
	public boolean remove( final E element ) {
		if ( element == null )
			throw new IllegalArgumentException( "Null is an invalid argument." );
		if ( !isModifiable )
			throw new IllegalArgumentException( "List has been appended with an UnmodifiableLinkList and items cannot be removed from the list." );

		final long change = changes;

		final boolean found = removeElement( findElement( element ) ) != null;

		if ( change != changes )
			throw new ConcurrentModificationException();
		incrementChange();

		return found;
	}

	/**
	 * Removes the head element from the list. 
	 * @return E - The removed element. 
	 * @throws ConcurrentModificationException Thrown if the list is
	 *			modified during the execution of this method. 
	 * @throws IllegalArgumentException Thrown if the list cannot
	 *			be modified.
	 */
	public E removeHead() {
		if ( !isModifiable )
			throw new IllegalArgumentException( "List is not modifiable." );
		final long change = changes;
		final E element;
		if ( size > 0 ) {
			element = head.getElement();
			if ( size == 1 )
				head = tail = null;				
			else {
				head = head.getTailwards();
				head.setHeadwards( null );
			}
			size--;
		} else
			element = null;
		if ( change != changes )
			throw new ConcurrentModificationException();
		incrementChange();
		return element;
	}

	/**
	 * Removes the tail element from the list. 
	 * @return E - The removed element. 
	 * @throws ConcurrentModificationException Thrown if the list is
	 *			modified during the execution of this method. 
	 * @throws IllegalArgumentException Thrown if the list cannot
	 *			be modified.
	 */
	public E removeTail() {
		if ( !isModifiable )
			throw new IllegalArgumentException( "List is not modifiable." );
		final long change = changes;
		final E element;
		if ( size > 0 ) {
			element = tail.getElement();
			if ( size == 1 )
				head = tail = null;				
			else {
				tail = tail.getHeadwards();
				tail.setTailwards( null );
			}
			size--;
		} else
			element = null;
		if ( change != changes )
			throw new ConcurrentModificationException();
		incrementChange();
		return element;
	}

	/**
	 * <p>An iterator to loop over the list from head-to-tail.</p>
	 * <p>Overrides the iterator from the super-class to add
	 * functionality to remove elements from the list.</p>
	 * @return Iterator<E>
	 * @see mgtaylor.datastructures.UnmodifiableLinkList#iterator()
	 * 		UnmodifiableLinkList.iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {
			ListElement current	= null;
			ListElement next		= head;

			@Override
			public boolean hasNext() {
				return next != null;
			}

			@Override
			public E next() {
				if ( hasNext() ) {
					current	= next;
					next	= next.getTailwards();
					return current.getElement();
				} else
					return null;
			}

			/**
			 * @throws ConcurrentModificationException Thrown if the list
			 * 		is modified during the execution of this method. 
			 * @throws IllegalArgumentException Thrown if the list cannot
			 *			be modified; there are no elements to remove; or if
			 *			the iterator has not been moved from the initial
			 *			point.
			 */
			@Override
			public void remove() {
				if ( size() == 0 )
					throw new IllegalArgumentException( "List is empty; there is nothing to remove." );
				if ( current == null )
					throw new IllegalArgumentException( "Iterator is before the first list item; call next() before removing an item." );
				if ( !isModifiable )
					throw new IllegalArgumentException( "List has been appended with an UnmodifiableLinkList and items cannot be removed from the list." );

				final long change = changes;

				ListElementStore elements = removeElement( current );
				if ( elements != null ) {
					current	= elements.getCurrent();
					next	= elements.getNext();
				} else {
					current = null;
					next	= null;
				}

				if ( change != changes )
					throw new ConcurrentModificationException();
				incrementChange();
			}
		};
	}

	/**
	 * <p>An iterator to loop over the list from tail-to-head.</p>
	 * <p>Overrides the iterator from the super-class to add
	 * functionality to remove elements from the list.</p>
	 * @return Iterator<E>
	 * @see mgtaylor.datastructures.UnmodifiableLinkList#iterator()
	 * 		UnmodifiableLinkList.iterator()
	 */
	@Override
	public Iterator<E> reverseIterator() {
		return new Iterator<E>() {
			ListElement current	= null;
			ListElement prev		= tail;

			@Override
			public boolean hasNext() {
				return prev != null;
			}

			@Override
			public E next() {
				if ( hasNext() ) {
					current	= prev;
					prev	= prev.getHeadwards();
					return current.getElement();
				} else
					return null;
			}

			/**
			 * @throws ConcurrentModificationException Thrown if the list
			 * 		is modified during the execution of this method. 
			 * @throws IllegalArgumentException Thrown if the list cannot
			 *			be modified; there are no elements to remove; or if
			 *			the iterator has not been moved from the initial
			 *			point.
			 */
			@Override
			public void remove() {
				if ( size() == 0 )
					throw new IllegalArgumentException( "List is empty; there is nothing to remove." );
				if ( current == null )
					throw new IllegalArgumentException( "Iterator is before the first list item; call next() before removing an item." );
				if ( !isModifiable )
					throw new IllegalArgumentException( "List is not modifiable." );

				final long change = changes;

				ListElementStore elements = removeElement( current );
				if ( elements != null ) {
					current	= elements.getNext();
					prev	= elements.getCurrent();
				} else {
					current = null;
					prev	= null;
				}

				if ( change != changes )
					throw new ConcurrentModificationException();
				incrementChange();
			}
		};
	}

	/**
	 * An internal data structure used to store a valid state
	 * for current & next elements when removing elements from
	 * the list.
	 * @author Martyn G Taylor
	 */
	protected class ListElementStore {
		/**
		 * The current position in the list. When an element is
		 * removed then the previous position is used. 
		 */
		private final ListElement current;
		/**
		 * The next element in the list.
		 */
		private final ListElement next;

		/**
		 * Initialiser for the store.
		 * @param current
		 * @param next
		 */
		public ListElementStore( final ListElement current, final ListElement next ) {
			this.current	= current;
			this.next		= next;
		}

		/**
		 * Gets the current element in the store.
		 * @return ListElement
		 */
		public ListElement getCurrent()	{ return current; }
		/**
		 * Gets the next element in the store.
		 * @return ListElement
		 */
		public ListElement getNext()	{ return next; }
	}
}
