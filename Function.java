public class Function
{	
	
	public int pow(int x, int n)
	{
		int result = 1;
		
		if(n!=0)
		{
			for(int i=0; i<n; i++)
			{
				result=result*x;
			}
		}
		
		return result;
	}
		
	public void analize_the_data(char[] data)
	{
				
		int N = 8;//幕次
		int index = 31;
		
		for(int i=0;;i++)
		{
						
			if(N>=0)
			{
				if(data[i] == '1')
				{
					simulator.target_address += pow(2, N);
					N--;				
				}
			
				else if(data[i] == '0')
				{
					N--;
				}			
			}
			
			else if(index>=0)
			{
				if(data[i] == '1')
				{
					simulator.current_data[index] = 1;
					--index;
				}
			
				else if(data[i] == '0')
				{
					simulator.current_data[index] = 0;
					--index;
				}
				
			}
			
			else
			{
				break;
			}
			
			
		}
	
	}
		
	public int four_bits_to_decimal(int[] b)
	{
		int d = b[0]*1 + b[1]*2 + b[2]*4 + b[3]*8;
		return d;
	}
	
	
	public int five_bits_to_decimal(int[] b)
	{			
		int d = 0;
		for(int i=0; i<5; i++)
		{
			d += b[i]*pow(2,i);
		}		 
		return d;
	}
	
	public int sixteen_bits_to_decimal(int[] b)
	{			
		int d = 0;
		for(int i=0; i<16; i++)
		{
			d += b[i]*pow(2,i);
		}		 
		return d;
	}
	
	public int twenty_bits_to_decimal(int[] b)//2's complements
	{			
		int d = 0;		
		
		if(b[19]==1)//判斷負數與否
		{
			
			int[] zero = new int[20];
			
			int[] n = simulator.alu.Sub(zero, b);
			
			for(int i=0; i<20; i++)
			{
				d += n[i]*pow(2,i);
			}
			
			return (0-d);
		}
		
		else
		{
			for(int i=0; i<20; i++)
			{
				d += b[i]*pow(2,i);
			}		 
			return d;
		}
		
	}
	
	public int twenty_eight_bits_to_decimal(int[] b)
	{
		int d = 0;
		for(int i=0; i<28; i++)
		{
			d += b[i]*pow(2,i);
		}		 
		return d;
	}
	
	public int eight_bits_to_decimal(int[] b)
	{
		int d = 0;
		for(int i=0; i<8; i++)
		{
			d += b[i]*pow(2,i);
		}		 
		return d;
	}
	
	public int thirty_two_bits_to_decimal(int[] b)
	{
		int d = 0;
		for(int i=0; i<32; i++)
		{
			d += b[i]*pow(2,i);
		}		 
		return d;
	}
		
	public int[] decimal_to_32bits(int d)
	{
		int[] b = new int[32];
		for(int i=0; i<32 ;i++)
		{
			b[i] = d%2;
			d = d/2;
		}
		return b;
	}
	
	public int[] sign_extend(int[] d)
	{
		int[] R = new int[32];
		
		for(int i=d.length; i<32; i++)
		{
			R[i] = d[d.length-1];
		}
		
		for(int i=0; i<d.length; i++)
		{
			R[i] = d[i];
		}
		
		return R;
	}
	
	
}