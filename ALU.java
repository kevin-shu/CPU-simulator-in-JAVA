public class ALU
{
	public int[] Add(int[] A, int B[])
	{
		int[] S = new int[A.length];
		int carry = 0;
		for(int i=0; i<A.length; i++)
		{
			if(A[i]!=B[i])
			{
				if(carry==1)
				{
					S[i]=0;
					carry = 1;
				}
				else
				{
					S[i]=1;
					carry = 0;
				}				
			}
			
			else if(A[i]==0 & B[i]==0)
			{
				S[i] = carry;
				carry = 0;
			}
			
			else
			{
				S[i] = carry;
				carry = 1;
			}
		}
		
		return S;
	}
	
	public int[] Sub(int[] A, int[] B)//A-B
	{
		int[] R;
		int[] temp = new int[A.length];
		for(int i=0; i<A.length; i++)
		{
			if(B[i]==1)
			{
				temp[i]=0;
			}
			else
			{
				temp[i]=1;
			}
		}
		
		int[] one = new int[A.length];
		one[0] = 1;
		
		int[] in_B = Add(temp,one);
		
		R = Add(A,in_B);
		
		return R;
	}
	
	
}