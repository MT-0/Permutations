package com.github.MT_0.permutations;

import java.util.ConcurrentModificationException;

/**
 * A linked list that makes the (normally private) data structures
 * containing links between elements public and adds functionality
 * to append elements to head/tailwards of a given list data
 * structure. 
 * @author MT0
 * @param <E>
 */
public class AccessibleUnmodifiableLinkList<E>
			extends UnmodifiableLinkList<E> {
	/**
	 * 
	 * @return
	 */
	public ListElement getHeadContainer()	{ return head; }

	/**
	 * 
	 * @return ListElement
	 */
	public ListElement getTailContainer()	{ return tail; }

	/**
	 * Overrides the superclass' findElement method to make it
	 * publicly accessible.
	 * @param element
	 * @return ListElement
	 */
	@Override
	public ListElement findElement( final E element ) {
		return super.findElement( element );
	}

	/**
	 * 
	 * @param listElement
	 * @param element
	 */
	public void appendHeadwardsOf( ListElement tailwardsElement, E element ) {
		if ( size() == 0 || tailwardsElement == head )
			addHead( element );
		else {
			long change = changes;
			ListElement le = new ListElement( element );
			ListElement headwardsElement = tailwardsElement.getHeadwards();
			le.setTailwards( tailwardsElement );
			le.setHeadwards( headwardsElement );
			tailwardsElement.setHeadwards( le );
			headwardsElement.setTailwards( le );
			size++;
			if ( change != changes )
				throw new ConcurrentModificationException();
			incrementChange();
		}
	}

	/**
	 * 
	 * @param headwardsElement
	 * @param element
	 */
	public void appendTailwardsOf( ListElement headwardsElement, E element ) {
		if ( size() == 0 || headwardsElement == tail )
			addTail( element );
		else {
			long change = changes;
			ListElement le = new ListElement( element );
			ListElement tailwardsElement = headwardsElement.getTailwards();
			le.setTailwards( tailwardsElement );
			le.setHeadwards( headwardsElement );
			tailwardsElement.setHeadwards( le );
			headwardsElement.setTailwards( le );
			size++;
			if ( change != changes )
				throw new ConcurrentModificationException();
			incrementChange();
		}
	}
}
