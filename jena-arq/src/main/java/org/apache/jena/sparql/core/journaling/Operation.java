package org.apache.jena.sparql.core.journaling;

/**
 * An encapsulation of some operation against a service of type <code>Upon</code> around a datum of type
 * <code>DataType</code>.
 *
 * @param <DataType> the type of data encapsulated
 * @param <Upon> the type of service upon which this operation acts
 */
public interface Operation<DataType, Upon> {

	/**
	 * @return the data encapsulated in this operation
	 */
	DataType data();

	/**
	 * Execute this operation against a given service
	 *
	 * @param service the service against which to execute
	 */
	void actOn(Upon service);

	/**
	 * An invertible {@link Operation}.
	 */
	public static interface InvertibleOperation<DataType, Upon> extends Operation<DataType, Upon> {

		/**
		 * Creates an inverse operation for this data. If this operation and then the result of {@link #inverse()} are
		 * run in sequence against a service without intervening operations, no change should result. Note that this may
		 * not be true if the inverse is run first.
		 *
		 * @return the inverse of this operation on the same data
		 */
		InvertibleOperation<DataType, Upon> inverse();

	}
}
