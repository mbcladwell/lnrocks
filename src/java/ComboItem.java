package lnrocks;
/**
 * This ComboItem provides the text string and index key upon selection
 *@param key and int used as primary key
 *@param value the String value shown in the dropdown

 */

class ComboItem
{
    private int key;
    private String value;

    public ComboItem(int key, String value)
    {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString()
    {
        return value;
    }

    public int getKey()
    {
        return key;
    }

    public String getValue()
    {
        return value;
    }
}
