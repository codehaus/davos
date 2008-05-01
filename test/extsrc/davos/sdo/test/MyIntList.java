package davos.sdo.test;

import java.util.List;
import java.util.ArrayList;

public class MyIntList
{
    List l = new ArrayList();

    public MyIntList(String value) throws NumberFormatException
    {
        if (value != null)
        {
            String nvalue = value.trim();
            if (!nvalue.equals(""))
            {
                String[] values = nvalue.split("\\s+");
                for (int i = 0; i < values.length; i++)
                {
                    int j = Integer.parseInt(values[i]);
                    l.add(j);
                }
            }
        }
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l.size() - 1; i++)
        {
            sb.append(l.get(i)).append(" ");
        }
        sb.append(l.get(l.size()-1));
        return sb.toString();
    }
}
