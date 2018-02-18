package azure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Elastic {
	
	// user-defined parameters
	private int eDTU_pool;
	private int storageTotal; // unit: GB
	private int nbDBMax;
	private int eDTUMax_DB;
	private int storageMax_DB; // unit: GB
	
	// system-defined parameters (config.)
	private List<int[]> poolList = new ArrayList<int[]>(); // plan, row
	//private List<int[]> extraStorageList = new ArrayList<int[]>(); // plan, extraStorage
	private int plan; // 1: Basic, 2: Standard, 3: Premium
	private double price;
	
	// utility param
	//private int planTmp; 
	//private double priceTmp;
	boolean extraDB = false;
	boolean extraStorage = false;
	

	/* Pricing Table for Basic, Standard and Premium Plan */
	// basic plan table: 8 criteria
	private int[] basic_eDTU_pool = {50,100,200,300,400,800,1200,1600};
	private int[] basic_storageInc_pool = {5,10,20,29,39,78,117,156};
	private int[] basic_nbDB_pool = {100,200,500};
	private int basic_eDTU_DB = 5;
	private double[] basic_price = {0.0851,0.1701,0.3402,0.5102,0.6803,1.3606,2.0408,2.7211};
	
	// standard plan table: 11 criteria
	private int[] standard_eDTU_pool = {50,100,200,300,400,800,1200,1600,2000,2500,3000};
	private int[] standard_storageInc_pool = {50,100,200,300,400,800,1170,1560,1950,2440,2930};
	private int[] standard_storageMax_pool = {500,750,1000,1250,1500,2000,2500,3000,3500,4000,4000};
	private int[] standard_nbDB_pool = {100,200,500};
	private int[] standard_eDTU_DB = {50,100,200,300,400,800,1200,1600,2000,2500,3000};
	private double[] standard_price = {0.1274,0.2548,0.5095,0.7643,1.019,2.038,3.057,4.076,5.095,6.3687,7.6425};
	private double standard_extraStoragePrice = 0.000099; // unit: GB
	
	// premium plan table: 10 criteria
	private int[] premium_eDTU_pool = {125,250,500,1000,1500,2000,2500,3000,3500,4000};
	private int[] premium_storageInc_pool = {250,500,750,1000,1500,2000,2500,3000,3500,4000};
	private int[] premium_storageMax_pool = {1000,1000,1000,1000,1500,2000,2500,3000,3500,4000};
	private int[] premium_nbDB_pool = {50,100};
	private int[] premium_eDTU_DB = {125,250,500,1000,1000,1750,1750,1750,1750,4000};
	private double[] premium_price = {0.7906,1.5812,3.1624,6.3248,9.4872,12.65,15.82,18.98,22.14,25.30};
	private double premium_extraStoragePrice = 0.000197; // unit: GB
	
	public Elastic() {}
	
	public Elastic(int eDTU_pool, int storageTotal, int nbDBMax, int eDTUMax_DB, int storageMax_DB) {
		this.eDTU_pool = eDTU_pool;
		this.storageTotal = storageTotal;
		this.nbDBMax = nbDBMax;
		this.eDTUMax_DB = eDTUMax_DB;
		this.storageMax_DB = storageMax_DB;
	}

	// decide initial plan based on the parameters that demand cannot be increased by increasing the number of pool
	// param: eDTU per pool, eDTU per DB, storage per DB
	public int decideInitialPlan() {
			
		if (eDTU_pool <= 1600 && eDTUMax_DB <= 5 && storageMax_DB <= 2) {
			plan = 1;
		} else if (eDTU_pool <= 3000 && eDTUMax_DB <= 3000 && storageMax_DB <= 1000) {
			plan = 2;
		} else if (eDTU_pool <= 4000 && eDTUMax_DB <= 4000 && storageMax_DB <= 1000) {
			plan = 3;
		} else if (eDTU_pool > 4000 && eDTUMax_DB > 4000 && storageMax_DB > 1000) {
			
			System.out.println("No plan available for you and we would provide you the premium plan with highest performance!");
			plan = 3;
			
		} else {
			System.out.println("This is an error!");
		}
		
		
		/*if (eDTU_pool <= 1600 && eDTUMax_DB <= 5 && storageMax_DB <= 2) {
			plan = 1;
		} else if ((eDTU_pool > 1600 && eDTU_pool <= 3000) && 
				(eDTUMax_DB > 5 && eDTUMax_DB <= 3000) && 
				(storageMax_DB > 2 && storageMax_DB <= 1000)) {
			plan = 2;
		} else if ((eDTU_pool > 3000 && eDTU_pool <= 4000) && 
				(eDTUMax_DB > 3000 && eDTUMax_DB <= 4000) && 
				(storageMax_DB > 2 && storageMax_DB <= 1000)) {
			plan = 3;
		} else {
			System.out.println("No plan available for you and we would provide you the premium plan with highest performance!");
			plan = 3;
		}*/
		
		return plan;
	}
	
	public void config() { 
		
		int currentRow = 0;	// price within the plan
		
		/* If all criteria (included storage) fits, select a price within the basic plan; otherwise, take a different deployment */
		if (this.getPlan() == 1) {
			
			currentRow = 0;
			currentRow = selectPrice(nbDBMax, basic_nbDB_pool, currentRow);

			if (currentRow != -1) {
				
				currentRow = selectPrice(eDTU_pool, basic_eDTU_pool, currentRow);
				int currentRowTmp = currentRow;
				//System.out.println(String.valueOf(eDTU_pool));
				currentRow = selectPrice(storageTotal, basic_storageInc_pool, currentRow);
				
				if (currentRow != -1) {
					// get the price if all performance requirements fit in a single pool in the basic plan
					price = basic_price[currentRow];
					
				} else {
					extraStorage = true;
					handleExtraStorage(currentRowTmp);
				}
			} else {
				extraDB = true;
				currentRow = selectPrice(eDTU_pool, basic_eDTU_pool, currentRow);
				// open new pool and handle storage
				handleExtraDB(currentRow, basic_storageInc_pool);
			}
		}

		/* If all criteria (included storage) fits, select a price within the standard plan; otherwise, take a different deployment */
		if (this.getPlan() == 2) {
			
			currentRow = 0;
			currentRow = selectPrice(nbDBMax, standard_nbDB_pool, currentRow);
			
			if (currentRow != -1) {
				
				currentRow = selectPrice(eDTUMax_DB, standard_eDTU_DB, currentRow);
				currentRow = selectPrice(eDTU_pool, standard_eDTU_pool, currentRow);
				int currentRowTmp = currentRow;
				currentRow = selectPrice(storageTotal, standard_storageInc_pool, currentRow);
				
				if (currentRow != -1) {
					// get the price if all performance requirements fit in a single pool in the basic plan
					price = standard_price[currentRow];
					
				} else {
					extraStorage = true;
					handleExtraStorage(currentRowTmp);
				}
			} else {
				extraDB = true;
				currentRow = selectPrice(eDTUMax_DB, standard_eDTU_DB, currentRow);
				currentRow = selectPrice(eDTU_pool, standard_eDTU_pool, currentRow);
				// open new pool and handle storage
				handleExtraDB(currentRow, standard_storageInc_pool);
			}
		}
		
		/* If all criteria (included storage) fits, select a price within the premium plan; otherwise, take a different deployment */
		if (plan == 3) {
			
			currentRow = 0;
			currentRow = selectPrice(nbDBMax, premium_nbDB_pool, currentRow);
			
			if (currentRow != -1) {
				extraDB = true;	// limit for nb of DB reached
			}

			currentRow = (selectPrice(eDTUMax_DB, premium_eDTU_DB, currentRow) == -1) ? 10 : selectPrice(eDTUMax_DB, premium_eDTU_DB, currentRow);
			currentRow = (selectPrice(eDTU_pool, premium_eDTU_pool, currentRow) == -1) ? 10 : selectPrice(eDTU_pool, premium_eDTU_pool, currentRow);
			currentRow = selectPrice(storageTotal, premium_storageInc_pool, currentRow);
			
			if (currentRow != -1) {
				// get the price if all performance requirements fit in a single pool in the basic plan
				price = premium_price[currentRow];
				
			} else {
				extraStorage = true;
				// 1. add additional storage with the same other performance
			}
		} 
	}
	
	// handle extra DB requirement by additional pool and handle with storage requirement
	public void handleExtraDB(int currentRow, int[] storageList ) {
		
		currentRow = selectPrice(eDTUMax_DB, standard_eDTU_DB, currentRow);
		currentRow = selectPrice(eDTU_pool, standard_eDTU_pool, currentRow);
		
		int nbPool = (int) Math.floor((double) (nbDBMax/500));
		
		for (int i = 0; i < nbPool; i++) {
			int[] nbPool_tmp = {plan,currentRow};
			poolList.add(nbPool_tmp);
		}
		
		int restNbDB = nbDBMax - 500*nbPool;
		
		if (restNbDB != 0) {	
			currentRow = selectPrice(restNbDB, basic_nbDB_pool, currentRow);
			int[] nbPool_tmp = {plan,currentRow};
			poolList.add(nbPool_tmp);
		}
		
		// check if the storage requirement meets the criteria in the table
		int storageIncluded_total = 0;
		
		for (int i = 0; i < poolList.size(); i++) {
			storageIncluded_total += storageList[poolList.get(i)[1]];
		}
		
		if (storageIncluded_total < storageTotal) {
			extraStorage = true;
			int extraStorageSize = storageTotal - storageIncluded_total;
			//handleExtraStorage
		}
	}
	
	// handle extra storage that is without extra DB requirements
	public void handleExtraStorage(int rowFitsOtherCriteria) {
		
		List<Double> pricesToCompare = new ArrayList<Double>();
		
		if(plan ==1) {
			pricesToCompare.add(improvePlan());
			pricesToCompare.add(addPoolStorage(basic_storageInc_pool, basic_storageInc_pool, basic_price, rowFitsOtherCriteria));
			price = Collections.max(pricesToCompare);
			
		} else if(plan == 2) {
			pricesToCompare.add(addStorage(standard_storageMax_pool, standard_price, rowFitsOtherCriteria));
			pricesToCompare.add(improvePlan());
			pricesToCompare.add(addPoolStorage(basic_storageInc_pool, basic_storageInc_pool, basic_price, rowFitsOtherCriteria));
			price = Collections.max(pricesToCompare);
		} else if(plan == 3) {
			pricesToCompare.add(addStorage(standard_storageMax_pool, standard_price, rowFitsOtherCriteria));
		}
	}
	
	// add additional storage within a pool. Applicable for Standard and Premium plan
	public double addStorage(int[] storageMax_pool, double[] priceList, int currentRow) {
				
		int NrRow = selectPrice(storageTotal, storageMax_pool, currentRow);	
		return (NrRow == -1) ? Double.MAX_VALUE : priceList[NrRow];
		
	}
	
	// improve the plan. Applicable for Basic and Standard plan
	public double improvePlan() {
		
		double priceTmp = Double.MAX_VALUE;
		int[] nbDB_pool = null;		
		int[] eDTU_DB = null;
		int[] eDTU_poolTmp = null;
		int[] storageInc_pool = null;
		int[] storageMax_pool = null;

		double[] priceList = null;
				
		if (storageTotal <= standard_storageMax_pool[standard_storageMax_pool.length-1]) {
			plan = 2;
			extraStorage = false;
			nbDB_pool = standard_nbDB_pool;	
			eDTU_DB= standard_eDTU_DB;
			eDTU_poolTmp= standard_eDTU_pool;
			storageInc_pool = standard_storageInc_pool;
			storageMax_pool = standard_storageMax_pool;
			priceList = standard_price;

		} else{
			plan = 3;
			extraStorage = false;
			nbDB_pool = premium_nbDB_pool;	
			eDTU_DB= premium_eDTU_DB;
			eDTU_poolTmp= premium_eDTU_pool;
			storageInc_pool = premium_storageInc_pool;
			storageMax_pool = premium_storageMax_pool;
			priceList = premium_price;
		}
		
		int NrRowTmp = 0, NrRow = 0;	
		NrRowTmp = selectPrice(nbDBMax, nbDB_pool, NrRowTmp);			
		NrRowTmp = selectPrice(eDTUMax_DB, eDTU_DB, NrRowTmp);
		NrRowTmp = selectPrice(eDTU_pool, eDTU_poolTmp, NrRowTmp);
		NrRow = selectPrice(storageTotal, storageInc_pool, NrRowTmp);
		
		if (NrRow != -1) {
			priceTmp = priceList[NrRow];
		} else {
			extraStorage = true;
			addStorage(storageMax_pool, priceList, NrRowTmp);
		}
				
		return (NrRow == -1) ? addStorage(storageMax_pool, priceList, NrRowTmp) : priceTmp;
	}
	
	// add more pool(s). Applicable for Basic and Standard plan
	public double addPoolStorage(int[] storageInc_pool, int[] storageMax_pool, double[] priceList, int rowFitsOtherCriteria) {
		
		int nbPool = (int) Math.floor((double) (storageTotal/storageMax_pool[storageMax_pool.length-1]));
		
		for (int i = 0; i < nbPool; i++) {
			int[] pool_tmp = {plan,storageMax_pool.length-1};
			poolList.add(pool_tmp);
		}
		
		int restStorage = storageTotal - storageMax_pool[storageMax_pool.length-1]*nbPool;

		if (restStorage != 0) {	
			int NrRow_rest = selectPrice(restStorage, storageInc_pool, rowFitsOtherCriteria);
			if(NrRow_rest == -1) {
				double priceTmp = addStorage(storageMax_pool, priceList, rowFitsOtherCriteria);
				for(int i = 0; i < priceList.length; i++) {
					if (priceList[i] == priceTmp) {
						NrRow_rest = i;
					} else {
						break;
					}
				}
				
			}
			
			int[] pool_tmp = {plan,NrRow_rest};
			poolList.add(pool_tmp);
		}
		
		double priceTmp = 0;
		for (int i =0; i < poolList.size(); i++) {
			priceTmp += basic_price[poolList.get(i)[1]];
		}

		return priceTmp;
		
	}
	
	/*public void handleExtraStorage_extraDB(int extraStorageSize, int[] storageInclList) {
				
		//	private List<int[]> nbPoolList = new ArrayList<int[]>(); // plan, row
			
		//int storageIncluded_total = 0;
		
		// add extra storage within a pool
		int extra_storage = extraStorageSize;
		for (int i = 0; i < nbPoolList.size(); i++) {

			if (nbPoolList.get(i)[0] == 1) { // Basic - scale up within the plan
				//int NrRow = selectPrice(extra_storage, storageInclList, nbPoolList.get(i)[1]);
				
				
			} else { // add extra storage with the same other performance (scale out within the plan)
				
			}
			
		}
			

		
		
		// 1. open new pool(s) [B1,S3]
		
		// 2. improve plan to S or P [B2,S2]
		
		// 3. add extra storage with the same other performance [S1,P]
		
		
	}*/
	
	public int selectPrice(int val, int[] criteriaArray, int currentRow) {
		
		int NrRow = 0;
		
		if (val > criteriaArray[criteriaArray.length-1]) {
			NrRow = -1;
		} else {
			for (int i = currentRow; i < criteriaArray.length; i++) {
				if (val <= criteriaArray[i]) {
					NrRow = i;
					break;
				}
				else {
					NrRow++;
				}
			}
		}
		
		return NrRow;
	}

	/* Getters and Setters */
	public int geteDTU_pool() {
		return eDTU_pool;
	}

	public void seteDTU_pool(int eDTU_pool) {
		this.eDTU_pool = eDTU_pool;
	}

	public int getStorageTotal() {
		return storageTotal;
	}

	public void setStorageTotal(int storage_pool) {
		this.storageTotal = storage_pool;
	}

	public int getNbDBMax() {
		return nbDBMax;
	}

	public void setNbDBMax(int maxNrDB_pool) {
		this.nbDBMax = maxNrDB_pool;
	}

	public int geteDTUMax_DB() {
		return eDTUMax_DB;
	}

	public void seteDTUMax_DB(int eDTUMax_DB) {
		this.eDTUMax_DB = eDTUMax_DB;
	}

	public int getStorageMax_DB() {
		return storageMax_DB;
	}

	public void setStorageMax_DB(int storageMax_DB) {
		this.storageMax_DB = storageMax_DB;
	}
	
	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getPlan() {
		return plan;
	}

	public void setPlan(int plan) {
		this.plan = plan;
	}

	// print plan
	public void printPlan(int planTmp) {
		
		if (planTmp == 0) {
			
			printPlan(plan);
		
		} else {
			
			switch (planTmp) {
			
				case 1:
					System.out.println("Basic Plan");
					break;
				case 2:
					System.out.println("Standard Plan");
					break;
				case 3:
					System.out.println("Premium Plan");
					break;
					
			}
			
		}
	}

	public void printConfig() {
		
		if (poolList.size() == 0) {
			printPlan(0);
		} else {
			
			System.out.println("You need to deploy several pools for a better price!");
			
			for (int i = 0; i < poolList.size(); i++) {
				printPlan(poolList.get(i)[0]);
				printPlan(poolList.get(i)[1]);
			}
			
		}
		
		System.out.println("Total price per hour = € " + String.valueOf(price));

		
	}
}
