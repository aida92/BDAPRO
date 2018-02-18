package azure;

public class AzureEstimator {

	public static void main(String[] args) {
		// user-defined parameters
		int eDTU_pool = 50;
		int storageTotal = 50; // unit: GB
		int maxNrDB = 100;
		int eDTUMax_DB = 50;
		int storageMax_DB = 1000; // unit: GB
		int time;
		
		System.out.println("Database Requirements:");
		System.out.println("eDTU/pool\tStorage\t\tMaxNrDB\t\teDTU/DB\t\tStorage/DB" + String.valueOf(eDTU_pool));
		System.out.print(String.valueOf(eDTU_pool) + "\t\t");
		System.out.print(String.valueOf(storageTotal) + "\t\t");
		System.out.print(String.valueOf(maxNrDB) + "\t\t");
		System.out.print(String.valueOf(eDTUMax_DB) + "\t\t");
		System.out.println(String.valueOf(storageMax_DB));

		System.out.println("=========================================================================================");

		Elastic getElasticPlan = new Elastic(eDTU_pool, storageTotal, maxNrDB, eDTUMax_DB, storageMax_DB);
		//System.out.println("Initial plan: " + String.valueOf(getElasticPlan.decideInitialPlan()));
		getElasticPlan.decideInitialPlan();
		getElasticPlan.config();
		//System.out.println("=========================================================================================");
		System.out.println("Suggested Deployment:");
		getElasticPlan.printConfig();


		//Elastic getElasticPlan = new Elastic(eDTU_pool, storageTotal, maxNrDB, eDTUMax_DB, storageMax_DB);
			
		
	}
}

//System.out.println("HEY : " + String.valueOf(currentRow));

