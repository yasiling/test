/**
 * 
 */
package award;

/**
 * @author loveholly519
 *
 */
public class GeneratePrize {

	/**
	 * @param args
	 */
	public int count1;
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		int rand = getRandom ();
//		//int prize;
//		
//		int isPrize = showPrize ( rand );
//		System.out.println(isPrize); 
//		
//	}
	public static int showPrize (int rnd){
		int prize;
		int count1 = 0;
		int count2 = 0;
		int count3 = 0;
		int count4 = 0;
		if (rnd == 1000){
			if (count1 < 1){
				prize = 1;
				count1++;
				return prize;
			}else 
				prize = 0;
				return prize;
		}else if (rnd >=300 & rnd <=320){
			if (count2 < 20){
				prize = 2;
				count2++;
				return prize;
			}else
				prize = 0;
				return prize;
			
		}else if (rnd >= 450 & rnd <= 500){
			if (count3 < 50){
				prize = 3;
				count3++;
				return prize;
			}else
				prize = 0;
				return prize;
			
		}else if (rnd >=0 & rnd <=200){
			if (count4 < 200){
				prize = 4;
				count4++;
				return prize;
			}
		}else 
			return 0;
		return 0;
		
	}
	
	public static int getRandom (){
		double i = Math.random();
		i *= 1000;
		int rnd = (int) i;
		return rnd;
	}

}
