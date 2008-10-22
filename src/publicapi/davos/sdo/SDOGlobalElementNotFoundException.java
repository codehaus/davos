package davos.sdo;

import javax.xml.namespace.QName;

public class SDOGlobalElementNotFoundException extends SDOSchemaValidationException
{
    private final QName _globalElementName;

    public SDOGlobalElementNotFoundException(String message, QName elName)
    {
        super(message);
        _globalElementName = elName;
    }

    public QName getGlobalElementName()
    {
        return _globalElementName;
    }
}
