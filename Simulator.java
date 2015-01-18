import java.io.*;
import java.util.*;
import java.util.Arrays.*;

public class simulator
{
	public static String memory_file_name = "input.txt";///////////////
	
	public static int target_address = 0;//將資料寫入MEMORY時所要存入的位置
	public static int[] current_data = new int[32];//目前所要存入MEMORY的資料
	
	public static Cache_Cell[][] cache = new Cache_Cell[2][4];
	
	public static boolean cache_valid = false;
	
	public static Recorder recorder = new Recorder();
	
	public static Register registers = new Register();
	public static Function function = new Function();
	public static Memory memory = new Memory();
	public static ALU alu = new ALU();
	static int Mode = 0;
	static int[] zero = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	
	
	
	
	
	public static void cache_access(int[] target, int address)
	{
		boolean BREAK = false;
		
		int index = (address/4)%(4);//見附注1
		int tag = address/16;//原理同上一行程式碼
		
		for(;;)
		{
																		
			
			if( cache[0][index].valid && (cache[0][index].tag==tag))
			{
				recorder.cache_reading_hit_times++;//紀錄hit次數
				
				for(int i=0; i<32; i++)
				{
					target[i] = cache[0][index].data[i];
				}
				
				break;
					
			}
				
				
				
				
			else if(      cache[1][index].valid 
		    	&& ( cache[1][index].tag == tag) )
			{
				recorder.cache_reading_hit_times++;//紀錄hit次數
				
				///////////為了確保1-st way一直都是最新資料的狀態，當找到的目標是2-st way時，要對1-st和2-st way做互換的動作////////
				
				boolean temp_dirt = cache[1][index].dirt;
				
				boolean temp_valid = cache[1][index].valid;
				
				int temp_tag = cache[1][index].tag;
				
				int[] temp_data = new int[32];
								
				cache[1][index].dirt = cache[0][index].dirt;
						
				cache[1][index].valid = cache[0][index].valid;
				
				cache[1][index].tag = cache[0][index].tag;
				
				cache[0][index].dirt = temp_dirt;
				
				cache[0][index].valid = temp_valid;
				
				cache[0][index].tag = temp_tag;				
				
													
				for(int i=0; i<32; i++)
				{
					temp_data[i] = cache[1][index].data[i];
				}				
				
				for(int i=0; i<32; i++)
				{
					cache[1][index].data[i] = cache[0][index].data[i];
				}
										
				for(int i=0; i<32; i++)
				{
					cache[0][index].data[i] = temp_data[i];
				}
				////////////////////////////////////////////////互換完成//////////////////////////////////////////////////////////											
				
				
				for(int i=0; i<32; i++)
				{	
					target[i] = cache[0][index].data[i];
				}	
					
				break;
									
			}
				
					
			else
			{
				recorder.cache_reading_miss_times++;//紀錄miss次數
				recorder.cache_reading_hit_times--;//因為在這個步驟後還會再讀一次CACHE，而第二次是必定是hit，所以要先減1
				
					
				///////將新的資料放進1-st way，而原本在1-st way的資料放進2-nd way，而2-nd way中較老舊的資料被淘汰掉//////	
							
				///////////////////////但在淘汰之前，必須將dirt被標記的entry寫回memory(write back)///////////////////////
				
				int old_addr = cache[1][index].tag*16 + index*4;
				
				if(cache[1][index].dirt)
				{
					for(int i=0; i<4; i++)
					{
						for(int j=0; j<8; j++)
						{
							memory.bytes[old_addr+i][j] = cache[1][index].data[8*i+j];
						}
					}	
				}
				
					
				cache[1][index].dirt = cache[0][index].dirt;
						
				cache[1][index].valid = cache[0][index].valid;
												
				cache[1][index].tag = cache[0][index].tag;
						
				for(int i=0; i<32; i++)
				{
					cache[1][index].data[i] = cache[0][index].data[i];
				}
						
						
				cache[0][index].dirt = false;
						
				cache[0][index].valid = true;
				
				cache[0][index].tag = tag;
										
				for(int i = 0;i<4;i++)//這個雙重FOR迴圈是為了將Memory的值寫入Cache，以便之後的運算
				{	
					for(int j=0; j<8; j++)//而因為Memory的單位是Byte(8-bits)，Cache的單位是Word(4-Bytes)，所以需要靠兩個FOR迴圈做4*8次的寫入
					{
						cache[0][index].data[i*8+j] = memory.bytes[address+i][j];								
					}
				}
						
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
				
			}	
		}
	}
	
	
	
	
	
	
	
	
	public static void write_cache(int[] source,int address)
	{
		int index = (address/4)%(4);//見附注1
		int tag = address/16;//原理同上一行程式碼
		
		for(;;)
		{
				
			if(      cache[0][index].valid 
		    	&& ( cache[0][index].tag == tag) )
			{
				recorder.cache_writing_hit_times++;//紀錄hit次數
				
				cache[0][index].dirt = true;
				
				for(int i=0; i<32; i++)
				{
					cache[0][index].data[i] = source[i];
				}
				
				break;					
			}
				
				
			else if(      cache[1][index].valid 
		    	&& ( cache[1][index].tag == tag) )
			{
				recorder.cache_writing_hit_times++;//紀錄hit次數
				
				cache[1][index].dirt = true;
				
				
				///////////為了確保1-st way一直都是最新資料的狀態，當找到的目標是2-st way時，要對1-st和2-st way做互換的動作////////
				
				boolean temp_dirt = cache[1][index].dirt;
				
				boolean temp_valid = cache[1][index].valid;
				
				int temp_tag = cache[1][index].tag;
				
				int[] temp_data = new int[32];
								
				cache[1][index].dirt = cache[0][index].dirt;
						
				cache[1][index].valid = cache[0][index].valid;
				
				cache[0][index].dirt = temp_dirt;
				
				cache[0][index].valid = temp_valid;
				
				cache[1][index].tag = cache[0][index].tag;
				
				cache[0][index].tag = temp_tag;
				
										
				for(int i=0; i<32; i++)
				{
					temp_data[i] = cache[1][index].data[i];
				}
														
				for(int i=0; i<32; i++)
				{
					cache[1][index].data[i] = cache[0][index].data[i];
				}
				
				for(int i=0; i<32; i++)
				{
					cache[0][index].data[i] = temp_data[i];
				}
				////////////////////////////////////////////////互換完成//////////////////////////////////////////////////////////	
				
				for(int i=0; i<32; i++)
				{	
					cache[1][index].data[i] = source[i];;
				}	
					
				break;
									
			}
				
					
			else
			{
				recorder.cache_writing_miss_times++;//紀錄miss次數
				recorder.cache_writing_hit_times--;//因為在這個步驟後還會再讀一次CACHE，而第二次是必定是hit，所以要先減1
				
					
				///////將新的資料放進1-st way，而原本在1-st way的資料放進2-nd way，而2-nd way中較老舊的資料被淘汰掉//////
					
				cache[1][index].dirt = cache[0][index].dirt;
						
				cache[1][index].valid = cache[0][index].valid;
				
				cache[1][index].tag = cache[0][index].tag;
				
						
				for(int i=0; i<32; i++)
				{
					cache[1][index].data[i] = cache[0][index].data[i];
				}		
						
						
				cache[0][index].dirt = true;
						
				cache[0][index].valid = true;
				
				cache[0][index].tag = tag;
										
				for(int i = 0;i<4;i++)//這個雙重FOR迴圈是為了將Memory的值寫入Cache，以便之後的運算
				{	
					for(int j=0; j<8; j++)//而因為Memory的單位是Byte(8-bits)，Cache的單位是Word(4-Bytes)，所以需要靠兩個FOR迴圈做4*8次的寫入
					{
						cache[0][index].data[i*8+j] = memory.bytes[registers.PC+i][j];								
					}
				}
						
				//////////////////////////////////////////////////////////////////////////////////////////////////////////
			}	
		}
	}
		
		
		
		
		
		
		
		
		
		
		
		
		
	
	public static void main(String[] args)
	{
				
		for(int i=0; i<2; i++)
		{
			for(int j=0; j<4; j++)
			{
				cache[i][j] = new Cache_Cell();
			}
		}
		
		
		
		//////////////////////////////這部份是將外部的資料寫入Memory/////////////////////////////////////
		try{
		BufferedReader memory_reader = null;
		memory_reader = new BufferedReader(new FileReader(memory_file_name));
	
		int index = 0;
		
		
			for(;;)
			{
				String data = memory_reader.readLine();
				
				if(data==null)
				{
					break;
				}
			
								
				char[] data_arr = data.toCharArray();
				
								
				if(data_arr.length==0)
				{
					continue;
				}
				
								
				if(data_arr[0]!='#')
				{
					function.analize_the_data(data_arr);
										
					for(int i=0;i<4;i++)
					{
						for(int j=0; j<8; j++)
						{
							memory.bytes[target_address+i][j] = current_data[i*8+j];
						}
					}
				}
				
				target_address = 0;
					
			}
		
	
		memory_reader.close();
		}

		catch(FileNotFoundException e)
		{System.out.println("File Not Found");}
		catch(IOException e)
		{System.out.println("IO Exception Happened");}
		///////////////////////////////////////寫入完成/////////////////////////////////////////////
	
	
		
	
		boolean HALT=false;
	
		
	
	
		/////////輸出一個紀錄執行過程的文字檔，以便使用者了解此程式的執行過程//////////////
		PrintWriter writer2 = null;
		try{
		writer2 = new PrintWriter(new FileOutputStream("execution_process_record.txt"));
		}
		catch(FileNotFoundException e){}
		///////////////////////////////////////////////////////////////////////////////////
		
		for(;;)
		{
										
			if(cache_valid)
			{
				cache_access(registers.instruction_register,registers.PC);
			}
			
			else
			{
				
				for(int i = 0;i<4;i++)//這個雙重FOR迴圈是為了將Memory的值寫入register，以便之後的運算
				{	
					for(int j=0; j<8; j++)//而因為Memory的單位是Byte(8-bits)，register的單位是Word(4-Bytes)，所以需要靠兩個FOR迴圈做4*8次的寫入
					{
						registers.instruction_register[8*i+j] = memory.bytes[registers.PC+i][j];
					}
				}
			}
			
			
			int instruction_31_28 = function.four_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,28,32)); //Op
				
			int instruction_27_24 = function.four_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,24,28)); //Rt
			
			int instruction_23_20 = function.four_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,20,24)); //Rs
			
			int instruction_19_16 = function.four_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,16,20)); //Rd
			
			int instruction_27_0 = function.twenty_eight_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,0,28)); //Address
			
			int instruction_19_0 = function.twenty_bits_to_decimal(java.util.Arrays.copyOfRange(registers.instruction_register,0,20)); //Offset
			
			
			///////////////////////////////////////////
			writer2.println("PC: "+registers.PC);    //印出目前的PC
			///////////////////////////////////////////
			
			
			switch (instruction_31_28)
			{
				
				case 0://ADDI
				
					writer2.println("ADDI R"+instruction_27_24+" R"+instruction_23_20+" "+instruction_19_0);
				
					int[] immd = function.sign_extend( java.util.Arrays.copyOfRange(registers.instruction_register,0,20) );;
										
					int[] sum = alu.Add(registers.register[instruction_23_20],immd);
					
					for(int i=0; i<32; i++)
					{
						registers.register[instruction_27_24][i] = sum[i];
					}
					
					recorder.clocks_recorder+=4;
					
					registers.PC += 4;			
				break;
				
				
				case 1://ADD
				
					writer2.println("ADD R"+instruction_19_16+" R"+instruction_23_20+" R"+instruction_27_24);
					
					sum = alu.Add(registers.register[instruction_27_24], registers.register[instruction_23_20]);
				
					for(int i=0; i<32; i++)
					{
						registers.register[instruction_19_16][i] = sum[i];
					}
					
					recorder.clocks_recorder+=4;
					
					registers.PC += 4;			
				break;
				
				
				case 2://SUB
				
					writer2.println("SUB R"+instruction_19_16+" R"+instruction_23_20+" R"+instruction_27_24);
					
					int[] rem = alu.Sub(registers.register[instruction_23_20], registers.register[instruction_27_24]);
					
					for(int i=0; i<32; i++)
					{
						registers.register[instruction_19_16][i] = rem[i];
					}
					
					recorder.clocks_recorder+=4;
					
					registers.PC += 4;			
				break;
				
				
				case 3://SLT
				
					writer2.println("SLT R"+instruction_19_16+" R"+instruction_23_20+" R"+instruction_27_24);
					
					rem = alu.Sub(registers.register[instruction_23_20], registers.register[instruction_27_24]);
					
					for(int i=0; i<32; i++)
					{
						registers.register[instruction_19_16][i] = 0;
					}
					
					if(rem[31]==1)
					{
						registers.register[instruction_19_16][0] = 1;					
					}
					else
					{
						registers.register[instruction_19_16][0] = 0 ;
					}
				
					recorder.clocks_recorder+=4;
					
					registers.PC += 4;			
				break;
				
				
				case 4://BNE
				
					writer2.println("BNE R"+instruction_23_20+" R"+instruction_27_24+" "+instruction_19_0);
					
					if(java.util.Arrays.equals(registers.register[instruction_27_24], registers.register[instruction_23_20]))
					{
						registers.PC += 4;
					}
					else
					{
						registers.PC += (instruction_19_0*4 + 4);
					}
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 5://JAL
				
					writer2.println("JAL "+instruction_27_0);
					
					registers.register[7] = function.decimal_to_32bits(registers.PC + 4);
					registers.PC = registers.PC - registers.PC%function.pow(2, 29) + instruction_27_0*4;
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 6://J
				
					writer2.println("J "+instruction_27_0);
					
					registers.PC = registers.PC - registers.PC%function.pow(2, 29) + instruction_27_0*4;
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 7://JR
				
					writer2.println("JR R"+instruction_27_24);
					
					int R = function.thirty_two_bits_to_decimal(registers.register[instruction_27_24]);
					registers.PC = R;
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 8://SW
				
					writer2.println("SW R"+instruction_27_24+" "+instruction_19_0+"(R"+instruction_23_20+")");
					
					int[] offset1 = function.sign_extend( java.util.Arrays.copyOfRange(registers.instruction_register,0,20) );
					
					int address1 = function.thirty_two_bits_to_decimal( alu.Add(registers.register[instruction_23_20],offset1) );
										
					if(cache_valid)
					{
						write_cache(registers.register[instruction_27_24], address1);
					}
					
					for(int i = 0;i<4;i++)
					{
						for(int j=0; j<8; j++)
						{
							memory.bytes[address1+i][j] = registers.register[instruction_27_24][8*i+j];
						}
					}						
				
					recorder.clocks_recorder+=5;
					
					registers.PC += 4;			
				break;
				
				
				case 9://LW
				
					writer2.println("LW R"+instruction_27_24+" "+instruction_19_0+"(R"+instruction_23_20+")");
				
					int[] offset2 = function.sign_extend( java.util.Arrays.copyOfRange(registers.instruction_register,0,20) );
										
					int address2 = function.thirty_two_bits_to_decimal( alu.Add(registers.register[instruction_23_20],offset2) );
					
					if(cache_valid)
					{
						cache_access(registers.register[instruction_27_24],address2);
					}
					else
					{
						for(int i = 0;i<4;i++)
						{
							for(int j=0; j<8; j++)
							{
								registers.register[instruction_27_24][8*i+j] = memory.bytes[address2+i][j];
							}
						}	
					}
						
					recorder.clocks_recorder+=4;
					
					registers.PC += 4;			
				break;
				
				
				case 10://TEQ
				
					writer2.println("TEQ R"+instruction_23_20+" R"+instruction_27_24+" "+instruction_19_0);
					
					if(java.util.Arrays.equals(registers.register[instruction_27_24], registers.register[instruction_23_20]))
					{
						registers.register[7] = function.decimal_to_32bits(registers.PC + 4);
						registers.PC = registers.PC - registers.PC%function.pow(2, 21) + instruction_19_0*4;
						Mode = 0;
					}
					else
					{
						registers.PC+=4;
					}
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 11://ERET
				
					writer2.println("ERET");
					
					registers.PC = function.thirty_two_bits_to_decimal( registers.register[7] );
					Mode = 1;
					
					recorder.clocks_recorder+=4;
					
				break;
				
				
				case 12://JU
				
					writer2.println("JU "+instruction_27_0);
					
					registers.register[7] = function.decimal_to_32bits(registers.PC + 4);
					registers.PC = registers.PC - registers.PC%function.pow(2, 29) + instruction_27_0*4;
					Mode = 1;
					
					recorder.clocks_recorder+=3;
					
				break;
				
				
				case 13://CAC
				
					writer2.println("CAC "+instruction_27_0);
				
					if(instruction_27_0==0)//關閉Cache
					{
						cache_valid=false;
					}
					
					else if(instruction_27_0==1)//開啟Cache
					{
						cache_valid=true;
					}
					
					else if(instruction_27_0==2)//Flush the cache
					{
						for(int i=0; i<2; i++)
						{
							for(int j=0; j<4; j++)
							{
								cache[i][j].valid =false;															
							}
						}
					}
					else
					{
					}
				
					recorder.clocks_recorder+=3;
									
					registers.PC += 4;			
				break;
				
				
				case 15://HALT
				
					writer2.println("HALT");
					
					HALT=true;
					
					recorder.clocks_recorder+=2;
										
				break;
			
			}
						
			/////////////////////////////////////////////////////////////////////////////**********
			for(int i=0; i<9; i++)
			{
				writer2.println("R"+i+": "+function.thirty_two_bits_to_decimal(registers.register[i]));				
			}
			
			writer2.println();
				
			writer2.println();
			/////////////////////////////////////////////////////////////////////////////
		
			if(HALT)
			{break;}
			
		}
				
		writer2.close();
		//////////////////////////
		
		
		PrintWriter info_writer = null;
		try{
		info_writer = new PrintWriter(new FileOutputStream("output.txt"));
		}
		catch(FileNotFoundException e){}
		
		info_writer.println("==================================================================");
		info_writer.println();
		
		info_writer.println("Register State:");
		info_writer.println();
		
		for(int i=0; i<9; i++)
		{
			info_writer.print("R"+i+": "+function.thirty_two_bits_to_decimal(registers.register[i])+"\t");
			for(int j=31; j>=0; j--)
			{
				info_writer.print(registers.register[i][j]);				
			}
			info_writer.println();		
		}
		
		info_writer.println();
		info_writer.println("==================================================================");
		info_writer.println();
		
		info_writer.println("Cache State:");
		info_writer.println();
		
		info_writer.println("------------------------------------------------------------------");
		
		info_writer.println("1-st way:");
		
		info_writer.println("         valid\tdirt\ttag\tdata");
			
		for(int i=0; i<4; i++)
		{
			info_writer.print("index "+i+": "+cache[0][i].valid+"\t"+cache[0][i].dirt+"\t"+cache[0][i].tag+"\t");
			
			for(int j=31; j>=0; j--)
			{
				info_writer.print(cache[0][i].data[j]);		
						
			}
			info_writer.println();		
		}
		
		info_writer.println("------------------------------------------------------------------");
		
		info_writer.println("2-nd way:");
		
		info_writer.println("         valid\tdirt\ttag\tdata");
			
		for(int i=0; i<4; i++)
		{
			info_writer.print("index "+i+": "+cache[1][i].valid+"\t"+cache[1][i].dirt+"\t"+cache[1][i].tag+"\t");
			
			for(int j=31; j>=0; j--)
			{
				info_writer.print(cache[1][i].data[j]);		
						
			}
			info_writer.println();		
		}
		
		info_writer.println("------------------------------------------------------------------");
				
		info_writer.println();
		info_writer.println("==================================================================");
		info_writer.println();
		
		info_writer.println("Memory State:");
		info_writer.println();
		
		for(int i=0; i<512; i++)
		{
			if(i<10)			
			{info_writer.print("M["+i+"]:  "+function.eight_bits_to_decimal(memory.bytes[i])+"\t");}
			
			else
			{info_writer.print("M["+i+"]: "+function.eight_bits_to_decimal(memory.bytes[i])+"\t");}
			
			for(int j=7; j>=0; j--)
			{
				info_writer.print(memory.bytes[i][j]);				
			}
			info_writer.println();		
		}		
		info_writer.println();
		
		info_writer.println("==================================================================");
		info_writer.println();
		
		info_writer.println("reading  hit  times: "+recorder.cache_reading_hit_times);
		info_writer.println("reading  miss times: "+recorder.cache_reading_miss_times);
		info_writer.println("reading  hitting  rate: " + (double)recorder.cache_reading_hit_times
														/(recorder.cache_reading_hit_times+recorder.cache_reading_miss_times)*100 +"%");
		info_writer.println();
		
		info_writer.println("------------------------------------------------------------------");
		info_writer.println();
		
		info_writer.println("writing  hit  times: "+recorder.cache_writing_hit_times);
		info_writer.println("writing  miss times: "+recorder.cache_writing_miss_times);
		info_writer.println("reading  hitting  rate: " + (double)recorder.cache_writing_hit_times
														/(recorder.cache_writing_hit_times+recorder.cache_writing_miss_times)*100 +"%");
		info_writer.println();
		
		info_writer.println("------------------------------------------------------------------");
		info_writer.println();
		
		info_writer.println("total  hit  times: "+ (recorder.cache_reading_hit_times+recorder.cache_writing_hit_times));
		info_writer.println("total  miss times: "+ (recorder.cache_reading_miss_times+recorder.cache_writing_miss_times));
		info_writer.println("total  hitting  rate: " + (double)(recorder.cache_reading_hit_times+recorder.cache_writing_hit_times)
													  /(recorder.cache_reading_hit_times+recorder.cache_writing_hit_times
														+recorder.cache_reading_miss_times+recorder.cache_writing_miss_times)*100 +"%");
		info_writer.println();
		
		info_writer.println("==================================================================");
		
		info_writer.println();
		
		info_writer.println("Execution time: "+recorder.clocks_recorder+"(clocks)");
		
		info_writer.println();
		
		info_writer.println("==================================================================");
		
		info_writer.close();
		
	}
	
	
}