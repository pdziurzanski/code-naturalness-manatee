package uk.ac.cs.york.manatee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import uk.ac.cs.york.manatee.ITokenizer.FullToken;

public class CodeNaturalness {

	String fileNameAbstractedSources;
    String fileNameNgrams;
	String fileNameConcreteSources;
	int SourcesNoToNgramise;
	int NoOfSourcesToAbstractise;
	int ngramSize;
	
	String sources[];

	public CodeNaturalness() {
		File configFile = new File("config.properties");
		 
		try {
		    FileReader reader = new FileReader(configFile);
		    Properties props = new Properties();
		    props.load(reader);
		 
		    fileNameAbstractedSources = props.getProperty("fileNameAbstractedSources");
		    fileNameNgrams = props.getProperty("fileNameNgrams");
		    fileNameConcreteSources = props.getProperty("fileNameConcreteSources");
		    ngramSize=Integer.parseInt(props.getProperty("ngramSize"));
			SourcesNoToNgramise=Integer.parseInt(props.getProperty("SourcesNoToNgramise"));
			NoOfSourcesToAbstractise=Integer.parseInt(props.getProperty("NoOfSourcesToAbstractise"));

		    
		    reader.close();
		} catch (FileNotFoundException ex) {
		} catch (IOException ex) {
		}
	}
	
	
    private Connection connect(String url) {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
            
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
        
    private void disconnect(Connection conn) {
    
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

	


        private void initialiseDbAbstractedSources(Connection connection) {
            String sql = "CREATE TABLE IF NOT EXISTS abstracted_source_file (\n"
            		+ "    hash VARCHAR NOT NULL, \n"
            		+ "    source BLOB NOT NULL, \n"
            		+ "    PRIMARY KEY (hash)\n"
            		+ "    );\n";
            
            try        		 
            {
            		Statement stmt = connection.createStatement();
                	stmt.execute(sql);
                	stmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        
        
        private void insertIntoDbAbstractedSources(Connection connection, String hash, String source) {
            String sql = "INSERT INTO abstracted_source_file(hash,source) VALUES(?,?)";
     
            try {
                PreparedStatement pstmt = connection.prepareStatement(sql);
                pstmt.setString(1, hash);
                pstmt.setString(2, source);
                pstmt.executeUpdate();
                pstmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        
        
        private boolean isHashErroneous(String hash) {
        	String[] erroneousHashes = {
        			"048f03df709548cc46e10989f2198c27846bce8f1883d110a39789d99c3905ec",
        			"162ce1076fa8faee1843c3e32881f58b43ec88d946fa2d1def278de35ede1c5e",
            		"162cd3ae7801791f8e9489ada932e580862f1feaf4bc34ed1f1ad6d0095597ad",
            		"08282cc0d375b8af619cb3ff14ffbd3e7aabefa788f2ed98f9e562b01f3a8aef",
            		"1101b185975a3594d562a7027d87b0ddc3c6814bd86d843420a5510d82b80004",
            		"162ce9ab01d63e5e1b32fdff164db66a2d7217f1ff885ca949ff44ac35e3c3ca",
            		"2443b7ae15ea229ba1df774245c66ee4677d958aa093bc4032ebda87cb15c9b5",
            		"2c8a33efd40f6ffd2b02347bb46da25dabec44a8ba453274510cbb81be89ca50",
            		"35cda88a002a359944bd119a4e0dd9d00d40ad2cd61434deb7bb9420007b3a42",
            		"3a184b5f5b5cba9802297a8025c5ba89f58a7de4d4f5c0dba8fa6a8386c66b80",
            		"40ae42b4a0557f4dc641c103bdf6feea011ea43d24ed9cceed3ad96b53660cc9",
            		"425801894dfd8eb2cfd20a3ed03ba8bdce2331fb992ef68c1d21a3ba3c3c0fb3",
            		"42b51c29a42613d09ac62514cc9599fee9d1a476d4fdd5aa893dd0e6d547abff",
            		"479f317607393fffbd6934a2860b0f636e46024ffd732bbb0aceaaca2c3068ff",
            		"56714244536630ac137f2202cf1a3fa3beb642813fe360941979de5711721965",
            		"57f2eb42fd599dfbcf076dbcc4cbf4c5150b7240a629e8ded860a2cb3a9baf18",
            		"591a71433c74236bb4fc11fe8da3037e0067e097219a839f88eaa67bc5ddccb8",
            		"6ee0b2590b9981bb70f02330698761dd6ba77ff7800999043a214f17227263f1",
            		"79813a97f53b97d710da8f12f1e152e7ae91729235f8f212ca40ecb6c27f7b53",
            		"7f3ee47951996adf04a43b6930bc4c77d10252ea80ed8d61bdc485a93a473d6e",
            		"7f8678bd9dfae92e7741778f667365ab3ba4d4d47c33779e167fb711928de436",
            		"801a4c498f61f20353158bb2e834aa4a96fb5b27890932cf294cfe63fda768fc",
            		"8101d574fdbb97092add8073f546e9f2f41eedc114f5fe3e27dae2ba73e33b25",
            		"88d717661dd69b0255ababdb552402f6d5efda735e09f482d91fa321ea4d95ae",
            		"8af616d578ec739d2c0a92aa8678c96fcc33929f4c3314a4eaeea4428045c277",
            		"971f438b2644a3654c1c121ebeda1ba01c68f13c750b4e29147b55b0ab100095",
            		"a15bdf720f7fe393e0b1aac18f74efbfbf816f493313e40e30dc603878df60af",
            		"a22a144dbc58e7eb8671ee978b613894000aff2060e76c324ac17086c2b2c660",
            		"b003f677acd97ce7477f55913cf24899255c8f15137cabc4451adbd60e291839",
            		"b61147618f6a13d92563c417f90a6ac05f568e615e621a62f3d450f9ade18c93",
            		"ccd39d317947ce29c2b952a86c8d7c19ef8f91e27a9b18224d8d045d33db8ed1",
            		"cd080101e8ac5de738876116a1e0dd73063224ed011479abad5c664bddcfe496",
            		"e03ea924307472f77cf3f49498bdd2a9ce314a82a323833e9652cc1df7dcc474"
            };
        	
        	boolean erroneousHashDetected = false;
        	for(String erroneousHash: erroneousHashes) {
        		if(hash.compareTo(erroneousHash)==0) {
        			erroneousHashDetected=true;
            		break;
        		}
        	}
        	return erroneousHashDetected;
        }
        
        private void addTokensFromSource(Set<String> tokens, String source) {
            JavaTokenizer javaTokenizer = new JavaTokenizer(false);

            List<FullToken> fullTokens = javaTokenizer.getTokenListFromCode(source.toCharArray());

            for (FullToken token : fullTokens) {
            	if (token.token.equals("\"StringLiteral\"")) {
            		tokens.add("StringLiteral");
            	}
            	else {
            		tokens.add(token.token);
            	}
            	
            }

        }
        
        

        
        public void parseNSourcesFromDb(Connection connSource, Connection connDest, Set<String> tokensAbstractedBag, int numberSelected){
  
        	String sql = "SELECT source_summary.hash, source_file.source FROM source_summary INNER JOIN source_file ON source_summary.hash=source_file.hash LIMIT "+ Integer.toString(numberSelected) +";";

        	
        	
        	try {
        		
        		Statement stmt  = connSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);
        		
        		ResultSet rs    = stmt.executeQuery(sql);

                while (rs.next()) {
                	
                	String hash = rs.getString("hash");
                	

				
                	if(isHashErroneous(hash)) {
                		continue;
                	}
            		ParseAST parseAST = new ParseAST();	
                	String source = rs.getString("source");
                	String sourceAbstracted = parseAST.abstractiseTokens(source);
                	addTokensFromSource(tokensAbstractedBag,sourceAbstracted);
                	insertIntoDbAbstractedSources(connDest, hash, sourceAbstracted);

                }
            	rs.close();

            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        	
        }        
        
        


        
        
        
    private void createDbAbstractedSources(String urlSource, String urlDest) {
    	CodeNaturalness dbSources = new CodeNaturalness();
        Connection conSource = connect(urlSource);

        Connection conDest = connect(urlDest);
        initialiseDbAbstractedSources(conDest);
        
        Set<String> tokensAbstractedBag = new HashSet<String>();
        dbSources.parseNSourcesFromDb(conSource,conDest,tokensAbstractedBag,NoOfSourcesToAbstractise);
        
        System.out.println("#Tokens abstracted: " + tokensAbstractedBag.size());
        System.out.println(tokensAbstractedBag);
        
        disconnect(conSource);
        disconnect(conDest);
    }

    private void deleteDb(String fileName) {
        File file = new File(fileName);
        file.delete();
    }
    
    private String fileName2SqlLiteUrl(String fileName) {
    	return "jdbc:sqlite:"+fileName;
    }
    
    
    
    public void concreteSources2Abstracted() {
    	
        deleteDb(fileNameAbstractedSources);
    	createDbAbstractedSources(fileName2SqlLiteUrl(fileNameConcreteSources),fileName2SqlLiteUrl(fileNameAbstractedSources));
    }
    
    
    
 
    
    public void initialiseNgrams(Connection connection, int ngramSize) {
        String sql = "CREATE TABLE IF NOT EXISTS ngrams (\n";
        for(int i=0;i<ngramSize;i++) {
        	sql+="    s"+i+" VARCHAR NOT NULL, \n";
        }
        sql+= "    count INT, \n";
        sql+= "    probability REAL, \n";
        sql+= "    PRIMARY KEY (";
        for(int i=0;i<ngramSize;i++) {
        	if(i<ngramSize-1) {
            	sql+="s"+i+", ";
        	}
        	else {
            	sql+="s"+i+")\n";
        	}
        	
        }
        sql+= "    );\n";

        try        		 
        {
        		Statement stmt = connection.createStatement();
            	stmt.execute(sql);
            	stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    
 
    private ArrayList<String> extractTokens(SortedMap<Integer, FullToken> tokensWithPositions) {
    	ArrayList<String> tokens = new ArrayList<String>();
    
    	for (SortedMap.Entry<Integer, FullToken> entry : tokensWithPositions.entrySet()) {
    		tokens.add(entry.getValue().token);
    	}
    	
    	return tokens;
    }
    
    
    private String composeKeys(ArrayList<String> ngram) {

    	String result="";
    	
    	int ngramSize = ngram.size();
    	
        for(int i=0;i<ngramSize;i++) {
    		String token=ngram.get(i);
    		
        	if (token.equals("\"StringLiteral\"")) {
        		token="StringLiteral";
       		}

        	if(i<ngramSize-1) {
            	result+="s"+i+"=\""+token+"\" AND ";
        	}
        	else {
            	result+="s"+i+"=\""+token+"\"";
        	}
        }
 	
    	
    	return result;
    }
    
    private boolean doesNgramExistInDb(Connection connection, ArrayList<String> ngram) {

    	boolean result = false;
    	String key = composeKeys(ngram);
    	


    	
        String sql = "SELECT EXISTS(SELECT 1 FROM ngrams WHERE " + key + ")";
        
        try        		 
        {
        		Statement stmt = connection.createStatement();
                ResultSet rs    = stmt.executeQuery(sql);
                int exist = 0;
                if ( rs.next() ) {
                	exist = rs.getInt(1);
                }
                if(exist==1) {
                	result=true;
                }
                rs.close();
                stmt.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    	
    	return result;
    }
    
    private int fetchCount(Connection connection, ArrayList<String> ngram) {
    	int result=0;

    	String key = composeKeys(ngram);
    	
        String sql = "SELECT SUM(count) FROM ngrams WHERE " + key + ";";
        
        try        		 
        {
        		Statement stmt = connection.createStatement();
                ResultSet rs    = stmt.executeQuery(sql);

                if ( rs.next() ) {
                	result = rs.getInt("sum(count)");

                }

                rs.close();
                stmt.close();
                
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    	
    	return result;
    }    

    
    private double fetchProbability(Connection connection, ArrayList<String> ngram) {
    	double result=0.0;
    	if(ngramNo==-1) {
        	ngramNo=getRowsNo(connection, "ngrams");
    	}

    	String key = composeKeys(ngram);
    	
        String sql = "SELECT probability FROM ngrams WHERE " + key + ";";
        
        try        		 
        {
        		Statement stmt = connection.createStatement();
                ResultSet rs    = stmt.executeQuery(sql);

                if ( rs.next() ) {
                	result = rs.getDouble("probability");

                }

                if(result==0.0) {
                	result = (double)(1)/(ngramNo); //Laplacian smoothing
                }
                rs.close();
                stmt.close();
                
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    	
    	return result;
    }   
    
    private ArrayList<String> removeLastElement(ArrayList<String> ngram) {
    	@SuppressWarnings("unchecked")
		ArrayList<String> result = (ArrayList<String>) ngram.clone();
    	result.remove(ngram.size()-1);
    	return result;
    	
    }
    
    int ngramNo=-1;  //cached value

    private double computeNgramProbability(Connection connection, ArrayList<String> ngram) {
    	double result = 0.0;
    	if(ngramNo==-1) {
        	ngramNo=getRowsNo(connection, "ngrams");
    	}
    	int counta1toan=fetchCount(connection,ngram);
    	int counta1toanminus1=fetchCount(connection,removeLastElement(ngram));
    	result = (double)(counta1toan+1)/(counta1toanminus1+ngramNo); //with Laplacian smoothing, it's why "+1" and "+ngramNo"
    	return result;
    }
    

    
    
    private void increaseNgramInstancesInDb(Connection connection, ArrayList<String> ngram) {
    	String key = composeKeys(ngram);
    	int count = fetchCount(connection,ngram);
    	count++;
    	
    	String sql = "UPDATE ngrams SET count = ? WHERE "+ key +";";
        
        
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            

            pstmt.setInt(1, count);

            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    }
    
    private void addNgramToDb(Connection connection, ArrayList<String> ngram) {
    	
   	
    	int ngramSize=ngram.size();
        String sql = "INSERT INTO ngrams(";
        
        
        for(int i=0;i<ngramSize;i++) {
        	if(i<ngramSize-1) {
            	sql+="s"+i+", ";
        	}
        	else {
            	sql+="s"+i+", count)";
        	}
        }

        sql+=" VALUES(";
        
        for(int i=0;i<ngramSize;i++) {
        	if(i<ngramSize-1) {
            	sql+="?, ";
        	}
        	else {
            	sql+="?, ?);";
        	}
        }
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            

            
            for(int i=0;i<ngramSize;i++) {

            	String token =  ngram.get(i);
            	if (token.equals("\"StringLiteral\"")) {
            		token="StringLiteral";
           		}            	
            	pstmt.setString(i+1, token);
            }
        	pstmt.setInt(ngramSize+1, 1);

            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    }
    int instance=0;
    private void increaseNgramInstances(Connection connection, ArrayList<String> ngram) {

    	if(doesNgramExistInDb(connection, ngram)) {
    		increaseNgramInstancesInDb(connection, ngram);
    	}
    	else {
    		addNgramToDb(connection, ngram);
    	}
    	instance++;
    }

    private void updateNgramProbabilityinDb(Connection connection, ArrayList<String> ngram, double probability) {
    	String key = composeKeys(ngram);
    	
    	String sql = "UPDATE ngrams SET probability = ? WHERE "+ key +";";
        
        try {
            PreparedStatement pstmt = connection.prepareStatement(sql);
            

            pstmt.setDouble(1, probability);

            pstmt.executeUpdate();
            pstmt.close();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    	
    }

    
    
    private void computeAllNgramsProbability(Connection connection, int ngramSize) {
    	String sql = "SELECT * FROM ngrams ;";
        
    	try {
    		
    		Statement stmt  = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);
            ResultSet rs    = stmt.executeQuery(sql);

            while (rs.next()) {
            	ArrayList<String> ngram = new ArrayList<String>();
            	for(int i=0;i<ngramSize;i++) {
                	String token = rs.getString("s"+i);
                	ngram.add(token);
            	}
            	double probability = computeNgramProbability(connection, ngram);
        		updateNgramProbabilityinDb(connection, ngram, probability);

            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    
    
    }
    
    
    private ArrayList<ArrayList<String>> source2ngrams(String sourceAbstracted, int ngramSize) {
		JavaTokenizer javaTokenizer = new JavaTokenizer(false);
		SortedMap<Integer, FullToken> tokensWithPositions = javaTokenizer.fullTokenListWithPos(sourceAbstracted.toCharArray());
		ArrayList<String> tokens = extractTokens(tokensWithPositions);
		ArrayList<ArrayList<String>> ngrams = Ngram.ngrams2(tokens, ngramSize);	
		return ngrams;
    }
    
    private void insertNgramsIntoDb(Connection connection, String hash, String sourceAbstracted, int ngramSize) {
    	

    	
    	
    		
    	ArrayList<ArrayList<String>> ngrams = source2ngrams(sourceAbstracted,ngramSize);
    		
    		for(ArrayList<String> ngram: ngrams) {
    			increaseNgramInstances(connection, ngram);
    		}
    		
    }
    	
    
    private int getRowsNo(Connection connection, String table) {
    	int result=0;
    	try {
            Statement st = connection.createStatement();
            ResultSet res = st.executeQuery("SELECT COUNT(*) FROM "+table);
            while (res.next()){
                result= res.getInt(1);
            }
            res.close();
            st.close();
        }
        catch (SQLException e){
            System.out.println(e.getMessage());
        }
    	
    	return result;
    }
    
    
    private void createNgramsFromMSourcesFromDb(Connection connSource, Connection connDest, int numberSelected, int ngramSize){
    	  
    	String sql = "SELECT hash, source FROM abstracted_source_file LIMIT "+ Integer.toString(numberSelected) +";";
      
        
    	try {
    		
    		Statement stmt  = connSource.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY,ResultSet.CLOSE_CURSORS_AT_COMMIT);
            ResultSet rs    = stmt.executeQuery(sql);
            int index=0;
            while (rs.next()) {
            	System.out.println("Source no.: " + index);
            	long startCpuTime = getCpuTime();
            	String hash = rs.getString("hash");
            	String sourceAbstracted = rs.getString("source");
            	
            	System.out.println("Abstracted source length: " + sourceAbstracted.length());
            	
            	insertNgramsIntoDb(connDest, hash, sourceAbstracted,ngramSize);
            	long endCpuTime = getCpuTime();
            	System.out.println("No of rows: " + getRowsNo(connDest, "ngrams"));
            	System.out.println("Execution time: " + ((double)(endCpuTime-startCpuTime)/ (1000)));
            	index++;
            }
            rs.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }        
    
    
    
    private void createNgrams(String urlSource, String urlDest, int ngramSize) {
    	CodeNaturalness dbSources = new CodeNaturalness();
        Connection conSource = connect(urlSource);

        Connection conDest = connect(urlDest);
        initialiseNgrams(conDest,ngramSize);
        
        int limit=SourcesNoToNgramise;
        dbSources.createNgramsFromMSourcesFromDb(conSource,conDest,limit,ngramSize);

        dbSources.computeAllNgramsProbability(conDest, ngramSize);

        
        
        disconnect(conSource);
        disconnect(conDest);
    }
    
    
    
    public void abstractedSources2Ngrams() {
        deleteDb(fileNameNgrams);
    	createNgrams(fileName2SqlLiteUrl(fileNameAbstractedSources),fileName2SqlLiteUrl(fileNameNgrams), ngramSize);
    }
    
    
    
    
    private static double log2(double d) {
        return Math.log(d)/Math.log(2.0);
     }
    
    public double computeCrossEntropy(String testCode, int ngramSize) {
    	double result = 0.0;
    	double sumLogProbability = 0.0;
    	
        Connection connection = connect(fileName2SqlLiteUrl(fileNameNgrams));

    	
		ParseAST parseAST = new ParseAST();	
    	String testCodeAbstracted=parseAST.abstractiseTokens(testCode);
    	
    	ArrayList<ArrayList<String>> testCodeNgrams= source2ngrams(testCodeAbstracted, ngramSize);
    	
		for(ArrayList<String> ngram: testCodeNgrams) {
			double probability=fetchProbability(connection, ngram);
			sumLogProbability+=log2(probability);
		}
		disconnect(connection);
		
		result = -1 * sumLogProbability/testCodeNgrams.size();


    	
    	return result;
    }

    

    
    
    public static long getCpuTime() {
    	return Instant.now().toEpochMilli();
    }

    
    public static void main(String[] args) {

    	
    	
    	System.out.println("Starting.");
		
    	try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
    	
    	long startCpuTime = getCpuTime();
    	
    	CodeNaturalness codeNaturalness = new CodeNaturalness();
    	codeNaturalness.concreteSources2Abstracted();
    	codeNaturalness.abstractedSources2Ngrams();
    	long endCpuTime = getCpuTime();

    	System.out.println("Execution time: " + ((double)(endCpuTime-startCpuTime)/ (1000)));
        
    }
	
    
}
