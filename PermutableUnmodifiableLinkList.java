package com.github.MT_0.permutations;

public class PermutableUnmodifiableLinkList<E> extends UnmodifiableLinkList<E> {
	/**
	 * The permutable list that will be re-ordered when cycling through permutations.
	 */
	private UnmodifiableLinkList<E> permutation		= new UnmodifiableLinkList<E>();

	/**
	 * The underlying list elements stored as an array.
	 * <ul>
	 * <li>The array is populated in the same order as the initial list; the K<sup>th</sup>
	 * element of the array maps to the K<sup>th</sup> element of the list.</li>
	 * <li>This allows for constant time access to and modification of the list structure
	 * using the array index.</li>
	 * </ul>
	 */
	private ListElement[]	elements			= null;

	/**
	 * An array storing the current position of each element in the elements array.
	 * <ul>
	 * <li>The index for this array is the initial order of the elements.</li>
	 * <li>Therefore the position of the element with the largest index will be stored
	 * in the last index of this array; conversely the position of the smallest indexed
	 * element will be in index 0 of the array.</li>
	 * </ul> 
	 */
	private int[]				elementPositions	= null;

	/**
	 * An array storing the current element index at a given position in the current
	 * permutation.
	 * <ul>
	 * <li>The array is initialised with the position indexes increasing sequentially
	 * as the elements are initially ordered sequentially.</li>
	 * <li>After cycling through permutations, if the value of the array at index M is
	 * N then the M<sup>th</sup> element of the permutation list has been given an index
	 * of N; this allows the algorithm to find the element index of a neighbouring element.</li>
	 * 
	 */
	private int[] 				positionToElements	= null;

	/**
	 * The total number of permutations; for an input list of N items there are N! permutations.
	 */
	private int					totalPermutations	= 0;

	/**
	 * The index of the current permutation; ranging between 0 and the total number of permutations.
	 */
	private int					currentPermutation	= 0;

//	private int					minChanges		= Integer.MAX_VALUE;
//	private int					maxChanges		= 0;
//	private int					totalChanges	= 0;

	/**
	 * Gets the total number of permutations.
	 * @return int	The total number of permutations.
	 */
	public int getTotalPermutations()	{
		if ( elementPositions == null && size() > 0 )
			generatePermutationData();
		return totalPermutations;
	}

	/**
	 * Returns the index of the current permutation.
	 * @return int	The index of the current permutation.
	 */
	public int getCurrentPermutation()	{ return currentPermutation; }

	/**
	 * Gets the current permutation of the base list.
	 * 
	 * @return E[]	The current permutation.
	 */
	public UnmodifiableLinkList<E> getPermutation() {
		if ( elementPositions == null && size() > 0 )
			generatePermutationData();
		return permutation;
	}

	/**
	 * Initialises the arrays of data used to iterate from one permutation to the next.
	 * <p>
	 * For a set with N elements and C constraints, this requires O(CN) time and O(N + C)
	 * memory.
	 */
	@SuppressWarnings("unchecked")
	private void generatePermutationData() {
		elements			= new UnmodifiableLinkList.ListElement[ size() ];
		positionToElements	= new int[ size() ];
		elementPositions	= new int[ size() ];
		totalPermutations	= 1;

		int i = 0;
		for ( ListElement element = permutation.head; element != null; element = element.getTailwards() ) {
			elements[i]				= element;
			positionToElements[i]	= i;
			elementPositions[i]		= i;
			totalPermutations		*= ++i;
		}

		meetsConstraints = true;
		for ( PermutationConstraint constraint: constraints )
			meetsConstraints &= constraint.testConstraintAfterGeneration();
	}

	/**
	 * Gets the next permutation of the cycle.
	 * <p>
	 * <b>Steinhaus-Johnson-Trotter Permutation Algorithm:</b>
	 * <ul>
	 * <li>Elements are initially ordered with an ascending index number from left-to-right.</li>
	 * <li>Elements are given a direction of travel, initially every element is travelling
	 * leftwards.</li>
	 * <li>Elements are mobile if, in their direction of travel, the neighbouring
	 * element is not the end of the list or an element with a higher index number.</li>
	 * <li>The element with the highest index is tested first, if it is mobile then it is
	 * swapped with the neighbouring element in its current direction of travel;</li>
	 * <li>If the element is not mobile then elements with successively lower indices are
	 * tested until a mobile element is found; else the element with the lowest index is
	 * swapped.</li>
	 * <li>Once the element has been swapped then the direction of travel is reversed for all
	 * elements with a higher index.</li>
	 * </ul>
	 * This algorithm follows a predictable pattern:
	 * <ul>
	 * <li>For a set with N elements, when cycling through permutations, the largest indexed
	 * element will be moved on occasions when the current permutation index (C) is not
	 * exactly divisible by N (C mod N ≠ 0).</li>
	 * <li>This results in the pattern that:
	 * <ul>
	 *		<li>The element with the largest index is moved from the right-to-left of the list,
	 *		each of the (N - 1) swaps it takes to achieve this movement represents a possible
	 *		permutation;</li>
	 *		<li>On the N<sup>th</sup> swap, when the element with the largest index is immobile
	 *		at the left end of the list, the next highest indexed mobile element is exchanged
	 *		with a neighbour and the direction of travel for the largest element is reversed;</li>
	 *		<li>On the (N + 1)<sup>th</sup> to (2N -1)<sup>th</sup> swaps, the largest element
	 *		is moved back from left-to-right; and</li>
	 *		<li>On the 2N<sup>th</sup> swap, when the element with the largest index is immobile
	 *		at the right of the list, again the next highest indexed mobile element is moved.</li>
	 *		<li>This process is repeated until all permutations have been cycled through.</li>
	 * </ul>
	 * <li>Therefore for (N - 1) out of N steps the element with the largest index will be
	 * moved and the direction of travel is moving left when (C mod 2N &lt; N) or right
	 * otherwise.</li>
	 * <li>The element with the second largest index will move when (C mod N = 0) and
	 * (C/N&nbsp;mod&nbsp;(N&nbsp;-&nbsp;1)&nbsp;≠ 0) and the direction of travel is left when
	 * (C/N mod 2(N - 1) &lt; (N - 1)) or right otherwise.</li>
	 * <li>In general, the K<sup>th</sup> indexed element of an N element set will move when
	 * C is exactly divisible by [∏<sub>M=(k+1)...N</sub>M] and C is not exactly divisible by K;
	 * the direction of travel is left if (C.K!/N! mod 2K &lt; K) or right otherwise.</li>
	 * <li>The element with index of 1 will only move when no other elements are mobile; this
	 * occurs when (for a set of N elements) the (N! - 1)<sup>th</sup> permutation is reached
	 * and the next permutation in the sequence is the 0<sup>th</sup> permutation, thus
	 * resetting the permutation order back to match the original order. The 1<sup>st</sup>
	 * element always moves left.</li>
	 * </ul>
	 * 
	 * Speed of the algorithm (for a list with N elements):
	 * <ul>
	 * <li>There are N! permutations.</li>
	 * <li>[N! - (N - 1)!] of the permutations are reached by swapping the largest
	 * (N<sup>th</sup>) element. This requires a single iteration of the loop.</li>
	 * <li>Of the (N - 1)! remaining permutations, [(N - 1)! - (N - 2)!] are reached by
	 * swapping the second largest element. This requires an iteration of the loop to
	 * determine that the largest element is immobile and a second iteration to swap the
	 * element.</li>
	 * <li>The K<sup>th</sup> element is moved [K! - (K - 1)!] times during one entire cycle
	 * through all possible permutations. Moving the K<sup>th</sup> element requires (N - K + 1)
	 * iterations through the loop (checking that N<sup>th</sup> down to (K+1)<sup>th</sup>
	 * elements are immobile) and one final iteration to swap the K<sup>th</sup> element.</li>
	 * <li>The 1<sup>st</sup> element is moved once during the entire cycle, to go from the
	 * [N! - 1]<sup>th</sup> permutation back to the 0<sup>th</sup> original permutation. To
	 * move the 1<sup>st</sup> element, the loop is iterated over [N - 1] times; this check
	 * that the N<sup>th</sup> down to 2<sup>nd</sup> elements are immobile before the
	 * 1<sup>st</sup> element is swapped (once the previous elements are all immobile there
	 * is no requirement to check for mobility of the 1<sup>st</sup> element as there is only
	 * one permutation where this occurs and the element is always mobile in this case).</li> 
	 * <li>For an N element set, to cycle through all possible (N!) permutations requires a
	 * total of [∑<sub>K=2...N</sub>K!] iterations through the loop and [N!] swaps.</li>
	 * <ul>
	 *		<li>At N=3, there is a total of 6 permutations and the loop is iterated a total of 8 times giving an
	 *		average cost of 1.33 iterations per permutation.</li>
	 *		<li>At N=4, there are totals of 24 permutations and 32 iterations, again, giving an average cost of
	 *		1.33 iterations per permutation.</li>
	 *		<li>At N=5, there are totals of 120 permutations and 152 iterations, giving an average cost of 1.27
	 *		iterations per permutation.</li>
	 *		<li>As N tends towards infinity the average cost tends towards 1 iteration per permutation following the
	 *		approximation Average Cost = 1 + <sup>1</sup>/<sub>N</sub>.</li>
	 * </ul></li>
	 * <li>The maximum cost for moving from one permutation to the next in the cycle is O(N).</li>
	 * </ul>
	 */
	public void nextPermutation() {
		if ( size() <= 1 )				return;
		if ( elementPositions == null )	generatePermutationData();

		int p = currentPermutation = (currentPermutation + 1) % totalPermutations;
		for ( int n = size(); n > 1; n-- ) {
			if ( p % n != 0 ) {
				swapElement( n - 1, (p % (2 * n)) < n );
				return;
			}
			p /= n;
		}
		swapElement( 0, true );
	}

	/**
	 * For a given permutation index (P) of a set of elements (&lt;1, ... N&gt;), the
	 * N<sup>th</sup> element will be in position (N - 1 - (P mod N)) if (P mod 2N &lt; N)
	 * or in position (P mod N) otherwise. This leaves (N - 1) unassigned positions.
	 * <p>
	 * For the general K<sup>th</sup> element, that will be put in position
	 * (K - 1 - (P.K!/N! mod K)) if (P.K!/N! mod 2K < K) or in position (P.K!/N! mod K)
	 * otherwise. If any elements with a higher index than K have an equal or lower
	 * position then the position of the K<sup>th</sup> element is shifted right by one
	 * for each of those elements.
	 * <p>
	 * For example, Permutation 23 of the set <1, 2, 3, 4, 5>:
	 * <style>
	 * table { border-collapse: collapse; width: 100%; }
	 * table, tr, td, th { border: 1px solid black; }
	 * td, th { padding 2px; }
	 * </style>
	 * <table cellpadding="2">
	 * <colgroup>
	 *	<col width="5%" />
	 *	<col width="10%" />
	 *	<col width="10%" />
	 *	<col width="15%" />
	 *	<col width="10%" />
	 *	<col width="30%" />
	 *	<col width="20%" />
	 * </colgroup>
	 * <tr>
	 * 	<th>K</th>
	 *	<th>P.K!/N!</th>
	 *	<th>P.K!/N! mod K</th>
	 *	<th>P.K!/N! mod 2K &lt; K</th>
	 *	<th>Initial<br />Position</th>
	 *	<th>Position<br />Shifts</th>
	 *	<th>Permutation</th>
	 * </tr>
	 * <tr>
	 * 	<td>5</td>
	 *	<td>23</td>
	 *	<td>3</td>
	 *	<td>True</td>
	 *	<td>4 - 3 = 1</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;null, 5, null, null, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>4</td>
	 *	<td>23/5 = 4</td>
	 *	<td>0</td>
	 *	<td>False</td>
	 *	<td>0</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;4, 5, null, null, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>3</td>
	 *	<td>23/20 = 1</td>
	 *	<td>1</td>
	 *	<td>True</td>
	 *	<td>2 - 1 = 1</td>
	 *	<td>1 → 2 (Due to 4<sup>th</sup> Element @ Position 0)<br />2 → 3 (5 @ 1)</td>
	 *	<td>&lt;4, 5, null, 3, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>2</td>
	 *	<td>23/60 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>1 - 0 = 1</td>
	 *	<td>1 → 2 (4 @ 0)<br />2 → 3 (5 @ 1)<br>3 → 4 (3 @ 3)</td>
	 *	<td>&lt;4, 5, null, 3, 2&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>1</td>
	 *	<td>23/120 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>0 - 0 = 0</td>
	 *	<td>0 → 1 (4 @ 0)<br />1 → 2 (5 @ 1)</td>
	 *	<td>&lt;4, 5, 1, 3, 2&gt;</td>
	 * </tr>
	 * </table>
	 * <p>
	 * The minimum shifts occurs when generating the 0<sup>th</sup> permutation:
	 * <table cellpadding="2">
	 * <colgroup>
	 *	<col width="5%" />
	 *	<col width="10%" />
	 *	<col width="10%" />
	 *	<col width="15%" />
	 *	<col width="10%" />
	 *	<col width="30%" />
	 *	<col width="20%" />
	 * </colgroup>
	 * <tr>
	 * 	<th>K</th>
	 *	<th>P.K!/N!</th>
	 *	<th>P.K!/N! mod K</th>
	 *	<th>P.K!/N! mod 2K &lt; K</th>
	 *	<th>Initial<br />Position</th>
	 *	<th>Position<br />Shifts</th>
	 *	<th>Permutation</th>
	 * </tr>
	 * <tr>
	 * 	<td>5</td>
	 *	<td>0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>4 - 0 = 4</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;null, null, null, null, 5&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>4</td>
	 *	<td>0/5 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>3 - 0 = 3</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;null, null, null, 4, 5&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>3</td>
	 *	<td>0/20 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>2 - 0 = 0</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;null, null, 3, 4, 5&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>2</td>
	 *	<td>0/60 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>1 - 0 = 1</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;null, 2, 3, 4, 5&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>1</td>
	 *	<td>0/120 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>0 - 0 = 0</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;1, 2, 3, 4, 5&gt;</td>
	 * </tr>
	 * </table>
	 * <p>
	 * The maximum shifts occurs when the permutation is generated such that the elements are in reverse order:
	 * <table cellpadding="2">
	 * <colgroup>
	 *	<col width="5%" />
	 *	<col width="10%" />
	 *	<col width="10%" />
	 *	<col width="15%" />
	 *	<col width="10%" />
	 *	<col width="30%" />
	 *	<col width="20%" />
	 * </colgroup>
	 * <tr>
	 * 	<th>K</th>
	 *	<th>P.K!/N!</th>
	 *	<th>P.K!/N! mod K</th>
	 *	<th>P.K!/N! mod 2K &lt; K</th>
	 *	<th>Initial<br />Position</th>
	 *	<th>Position<br />Shifts</th>
	 *	<th>Permutation</th>
	 * </tr>
	 * <tr>
	 * 	<td>5</td>
	 *	<td>64</td>
	 *	<td>4</td>
	 *	<td>True</td>
	 *	<td>4 - 4 = 0</td>
	 *	<td>&nbsp;</td>
	 *	<td>&lt;5, null, null, null, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>4</td>
	 *	<td>64/5 = 12</td>
	 *	<td>0</td>
	 *	<td>False</td>
	 *	<td>0</td>
	 *	<td>0 → 1 (5 @ 0)</td>
	 *	<td>&lt;5, 4, null, null, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>3</td>
	 *	<td>64/20 = 3</td>
	 *	<td>0</td>
	 *	<td>False</td>
	 *	<td>0</td>
	 *	<td>0 → 1 (5 @ 0)<br />1 → 2 (4 @ 1)</td>
	 *	<td>&lt;5, 4, 3, null, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>2</td>
	 *	<td>64/60 = 1</td>
	 *	<td>1</td>
	 *	<td>True</td>
	 *	<td>1 - 1 = 0</td>
	 *	<td>0 → 1 (5 @ 0)<br />1 → 2 (4 @ 1)<br />2 → 3 (3 @ 2)</td>
	 *	<td>&lt;5, 4, 3, 2, null&gt;</td>
	 * </tr>
	 * <tr>
	 * 	<td>1</td>
	 *	<td>64/120 = 0</td>
	 *	<td>0</td>
	 *	<td>True</td>
	 *	<td>0 - 0 = 0</td>
	 *	<td>0 → 1 (5 @ 0)<br />1 → 2 (4 @ 1)<br />2 → 3 (3 @ 2)<br />3 → 4 (2 @ 3)</td>
	 *	<td>&lt;5, 4, 3, 2, 1&gt;</td>
	 * </tr>
	 * </table>
	 * <ul>
	 * <li>The maximum number of position assignments plus shifts is [½N² + ½N].</li>
	 * <li>The minimum number of position assignments plus shifts is N.</li>
	 * <li>The average number of assignments per permutation, over a complete cycle
	 * through N! permutations, is [¼N² + ¾N].</li>
	 * </ul>
	 * 
	 * @param p		The index of the permutation to jump to.
	 */
	public void jumpToPermutation( final int p ) {
		if ( size() <= 1 )				return;
		if ( elementPositions == null )	generatePermutationData();

		currentPermutation = p % totalPermutations;
		if ( currentPermutation < 0 )
			currentPermutation += totalPermutations;

		int q = currentPermutation;
		final AccessibleUnmodifiableLinkList<Integer> order = new AccessibleUnmodifiableLinkList<Integer>();
//		int k = 0;
		ListElement listElement = tail;
		for ( int n = size(); n >= 1; n-- ) {
			int position = ( q % (2 * n) < n )?(n - (q % n) - 1):(q % n);
//			k++;
			UnmodifiableLinkList<Integer>.ListElement phe = order.getHeadContainer();
			while ( phe != null && phe.getElement() <= position ) {
				phe = phe.getTailwards();
				position++;
//				k++;
			}
			if ( phe == null )
				order.addTail( position );
			else
				order.appendHeadwardsOf( phe,  position );
			positionToElements[ position ]	= n - 1;
			elementPositions[ n - 1 ]		= position;
			elements[ position ].setElement( listElement.getElement() );
			listElement = listElement.getHeadwards();
			q /= n;
		}

		meetsConstraints = true;
		for ( PermutationConstraint constraint: constraints )
			meetsConstraints &= constraint.testConstraintAfterGeneration();
//		minChanges = Math.min( k,  minChanges );
//		maxChanges = Math.max( k,  maxChanges );
//		totalChanges += k;
	}

	/**
	 * Gets the previous permutation of the cycle.
	 * @see #nextPermutation()
	 */
	public void previousPermutation() {
		if ( size() <= 1 )				return;
		if ( elementPositions == null )	generatePermutationData();

		int p = currentPermutation;
		currentPermutation = currentPermutation == 0?totalPermutations - 1:currentPermutation - 1;
		for ( int n = size(); n > 1; n-- ) {
			if ( p % n != 0 ) {
				swapElement( n - 1, (p % (2 * n)) >= n );
				return;
			}
			p /= n;
		}
		swapElement( 0, false );
	}

	/**
	 * Swaps the position of the element at the given index and the element
	 * neighbouring it in the specified direction.
	 * <p>
	 * Constraint checking is O(C) where C is the number of constraints.
	 * 
	 * @param mobileElement	The index of the mobile element that is being swapped.
	 * @param direction		Whether the element at the given index is moving left (true)
	 * 						or right (false).
	 */
	protected void swapElement( final int mobileElement, final boolean direction ) {
		final int mobileElementPosition	= elementPositions[ mobileElement ];
		final int swapElementPosition	= mobileElementPosition + (direction?-1:1);
		final int swapElement			= positionToElements[ swapElementPosition ];

		elementPositions[ mobileElement ]			= swapElementPosition;
		elementPositions[ swapElement ]				= mobileElementPosition;
		positionToElements[ mobileElementPosition ]	= swapElement;
		positionToElements[ swapElementPosition ]	= mobileElement;
		
		final E temp = elements[ mobileElementPosition ].getElement();
		elements[ mobileElementPosition ].setElement( elements[ swapElementPosition ].getElement() );
		elements[ swapElementPosition ].setElement( temp );
		
		meetsConstraints = true;
		final E before;
		final E after;
		if ( direction ) {
			before	= elements[ swapElementPosition ].getElement();
			after	= elements[ mobileElementPosition ].getElement();
		} else {
			before	= elements[ mobileElementPosition ].getElement();
			after	= elements[ swapElementPosition ].getElement();
		}
		for ( final PermutationConstraint constraint: constraints )
			meetsConstraints &= constraint.testConstraintAfterSwap( before, after );
		handleSwap( before, after );
	}

	/**
	 * Method stub to allow sub-classes to handle swaps.
	 * @param earlier	The element swapped to an earlier position in the list.
	 * @param later		The element swapped to an later position in the list.
	 */
	protected void handleSwap( final E earlier, final E later ) {}
	
	@Override
	public void addTail( E element ) {
		if ( elements != null )	throw new IllegalArgumentException( "Permutation has been generated and elements cannot be added to the list." );
		super.addTail( element );
		permutation.addTail( element );
	}

	@Override
	public void addHead( E element ) {
		if ( elements != null )	throw new IllegalArgumentException( "Permutation has been generated and elements cannot be added to the list." );
		super.addHead( element );
		permutation.addHead( element );
	}

	@Override
	public void addAllTail( UnmodifiableLinkList<E> list ) {
		if ( elements != null )	throw new IllegalArgumentException( "Permutation has been generated and elements cannot be added to the list." );
		super.addAllTail( list );
		permutation.addAllTail( list );
	}

	@Override
	public void addAllHead( UnmodifiableLinkList<E> list ) {
		if ( elements != null )	throw new IllegalArgumentException( "Permutation has been generated and elements cannot be added to the list." );
		super.addAllHead( list );
		permutation.addAllHead( list );
	}

	/*********************************************************************************
	 * Constraints
	 ********************************************************************************/

	/**
	 * Flag to check whether the permutation meets the constraints.
	 */
	protected boolean meetsConstraints = true;

	/**
	 * The constraints that the permutation must meet to be valid. 
	 */
	protected final UnmodifiableLinkList<PermutationConstraint> constraints = new UnmodifiableLinkList<PermutationConstraint>();

	/**
	 * Adds a constraint to the permutation.
	 * 
	 * @param before
	 * @param after
	 */
	public void addConstraint( final E before, final E after ) {
		constraints.addTail( new PermutationConstraint( before, after ) );
	}

	/**
	 * Checks whether the permutation meets all the constraints.
	 * @return boolean	Whether the permutation meets all constraints.
	 */
	public boolean isValidPermutation()		{ return meetsConstraints; }

	/**
	 * Class storing a constraint on the permutation restricting the permutation to be
	 * valid only when a given element appears in the permutation before another given
	 * element.
	 * @author Martyn Taylor
	 */
	protected class PermutationConstraint {
		private final E elementBefore;
		private final E elementAfter;
		private boolean constraintMatched = true;

		/**
		 * Adds a constraint to the permutation that one element should always be
		 * earlier in the order than another element.
		 * @param before	The element that should be earlier in the permutation.
		 * @param after		The element that should be later in the permutation.
		 */
		private PermutationConstraint( final E before, final E after ) {
			if ( before == null )	throw new IllegalArgumentException( "Constrained element cannot be null." );
			if ( after == null )	throw new IllegalArgumentException( "Constrained element cannot be null." );
			if ( before == after )	throw new IllegalArgumentException( "Constrained elements cannot be equal." );
			elementBefore	= before;
			elementAfter	= after;
		}

		/**
		 * Checks the elements being swapped to see if they match the constrained
		 * elements and if so, whether they are in the correct order.
		 * <ul>
		 * <li>If the swapped elements match the constrained elements and they are in
		 * the correct order after the swap then the constraint has been met;</li>
		 * <li>If the swapped elements match the constrained elements and they are in
		 * the reverse order then the constraint is not matched and the permutation is
		 * invalid; and</li>
		 * <li>If one or more elements does not match the constrained elements then
		 * the swap will not change the relative order of the constrained elements
		 * and the validity of the permutation will be the same as the previous
		 * permutation with regards to this constraint.</li>
		 * </ul>
		 * 
		 * @param swapFirst		The swapped element that is now earlier in the permutation.
		 * @param swapSecond	The swapped element that is now later in the permutation.
		 * @return boolean		Whether the constraint has been matched.
		 */
		private boolean testConstraintAfterSwap( final E swapFirst, final E swapSecond ) {
			if ( swapFirst == elementBefore && swapSecond == elementAfter )
				constraintMatched = true;
			else if ( swapFirst == elementAfter && swapSecond == elementBefore )
				constraintMatched = false;
			return constraintMatched;
		}

		/**
		 * Iterates through the list of elements to find which if the constrained
		 * elements is first in the list. If the element constrained to be earlier in
		 * the permutation is found first then the constraint is matched; otherwise, the
		 * permutation is invalid.

		 * @return boolean	Whether the constraint has been matched.
		 */
		private boolean testConstraintAfterGeneration() {
			for ( final E element: PermutableUnmodifiableLinkList.this.getPermutation() ) {
				if ( element == elementBefore ) {
					constraintMatched = true;
					return true;
				} else if ( element == elementAfter ) {
					constraintMatched = false;
					return false;
				}
			}
			throw new IllegalArgumentException( "No constraint elements found." );
		}
	}
}