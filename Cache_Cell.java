/*此class是用來建立Cache的資料形式，以便在CPU內建立Chache群*/

public class Cache_Cell
{
	public boolean valid=false;
	public boolean dirt=false;
	public int tag = 0;
	public int data[] = new int[32];
	
}