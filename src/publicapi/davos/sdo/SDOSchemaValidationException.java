package davos.sdo;

public class SDOSchemaValidationException extends SDOXmlException
{
    public SDOSchemaValidationException()
    {
        super();
    }

    public SDOSchemaValidationException(Throwable cause)
    {
        super(cause);
    }

    public SDOSchemaValidationException(String message)
    {
        super(message);
    }

    public SDOSchemaValidationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
