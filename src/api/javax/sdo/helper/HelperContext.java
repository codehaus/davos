package javax.sdo.helper;

/**
 * This interface represents a helper execution context.
 * The set of helpers returned by the methods in this interface have visibility
 * to the same SDO metadata, that is, they execute in the same "scope".
 */
public interface HelperContext
{
  /**
   * Gets the CopyHelper to use in this context.
   * @return a CopyHelper object
   */
  CopyHelper getCopyHelper();

  /**
   * Gets the DataFactory to use in this context.
   * @return a DataFactory object
   */
  DataFactory getDataFactory();

  /**
   * Gets the DataHelper to use in this context.
   * @return a DataHelper object
   */
  DataHelper getDataHelper();

  /**
   * Gets the EqualityHelper to use in this context.
   * @return an EqualityHelper object
   */
  EqualityHelper getEqualityHelper();
  
  /**
   * Gets the TypeHelper to use in this context.
   * @return a TypeHelper object
   */
  TypeHelper getTypeHelper();

  /**
   * Gets the XMLHelper to use in this context.
   * @return an XMLHelper object
   */
  XMLHelper getXMLHelper();
  
  /**
   * Gets the XSDHelper to use in this context.
   * @return an XSDHelper object
   */
  XSDHelper getXSDHelper();
}
