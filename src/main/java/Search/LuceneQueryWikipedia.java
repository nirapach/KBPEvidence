package Search;

import PojoClasses.Nell_Extracts;
import PojoClasses.Wiki_Extracts;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LuceneQueryWikipedia {

    private static String TEXT = "extract";
    private static final String COMMA_DELIMITER = ",";
    //extracts members
    private static final int relation_id = 0;
    private static final int mention_id = 1;
    private static final int object_id = 2;

    private static final String NEW_LINE_SEPARATOR = "\n";
    //CSV file header
    private static final String FILE_HEADER = "relation,mention,object,support_extracted_string";

    public void search_query(File indexdirectory, File datadirectory, String resultdirectory, String filetype) throws IOException, ParseException {

        //csv files to get the wuery terms
        File[] files = datadirectory.listFiles();
        //System.out.println(files.length);
        BufferedReader fileReader = null;
        FileWriter fileWriter = null;

        //lucene queries parser and reader
        Path path = Paths.get(String.valueOf(indexdirectory));

        IndexReader reader = DirectoryReader.open(FSDirectory.open(path));

        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(FSDirectory.open(path)));

        QueryParser parser = new QueryParser(TEXT, new StandardAnalyzer());

        try {

            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory() && !files[i].isHidden() && files[i].canRead() && files[i].exists()) {

                    //Create the file reader
                    List<Wiki_Extracts> extracts = new ArrayList();
                    String old_filename = files[i].getAbsolutePath();
                    int ch = old_filename.lastIndexOf("\\");
                    String concat_filename = old_filename.substring(ch + 1, old_filename.length() - 4);
                    System.out.println(concat_filename);
                    fileReader = new BufferedReader(new FileReader(files[i]));
                    String line = "";
                    //Read the CSV file header to skip it
                    fileReader.readLine();

                    HashMap<String, String> eliminatingAllDuplicates = new HashMap<String, String>();
                    while ((line = fileReader.readLine()) != null) {

                        //Get all tokens available in line
                        line = line + ",";
                        String[] tokens = line.split(COMMA_DELIMITER);
                        //System.out.println(tokens.length);

                        if (tokens.length > 2 && tokens.length < 4) {
                            //Create a new extract object and fill his  data
                            tokens[mention_id] = tokens[mention_id].replace("_", " ");
                            tokens[object_id] = tokens[object_id].replace("_", " ");
                            tokens[mention_id] = tokens[mention_id].replaceAll("[-+.^:\\/()!']", " ");
                            tokens[object_id] = tokens[object_id].replaceAll("[-+.^:,\\/()!']", " ");
                            if (tokens[mention_id] != null && tokens[object_id] != null) {
                                Wiki_Extracts wiki_ex = new Wiki_Extracts(tokens[relation_id], tokens[mention_id], tokens[object_id]);
                                extracts.add(wiki_ex);
                            }
                        }
                    }
                    //create File object
                    File file = new File(resultdirectory + "Wikipedia_" + concat_filename + "_Evidence" + "." + filetype);
                    String filename = file.getAbsolutePath();
                    //File writer
                    fileWriter = new FileWriter(filename);
                    //Write the CSV file header
                    fileWriter.append(FILE_HEADER.toString());
                    //Add a new line separator after the header
                    fileWriter.append(NEW_LINE_SEPARATOR);

                    System.out.println("Writing Text File for " + concat_filename);
                    //HashMap<String, String> eliminatingPairDuplicates = new HashMap<String, String>();
                    for (Wiki_Extracts wiki_exs : extracts) {
                        //lucene phrase query

                        PhraseQuery queryMentions = new PhraseQuery();
                        //queryMentions.setSlop(1);
                        String[] mentionWords = wiki_exs.getMentions().split(" ");
                        for (String mentionWord : mentionWords) {
                            queryMentions.add(new Term(TEXT, mentionWord));
                        }
                        PhraseQuery queryObjects = new PhraseQuery();
                        //queryObjects.setSlop(1);
                        String[] objectWords = wiki_exs.getObject().split(" ");
                        for (String objectWord : objectWords) {
                            queryObjects.add(new Term(TEXT, objectWord));
                        }

                        BooleanQuery booleanQuery = new BooleanQuery();
                        booleanQuery.add(queryMentions, BooleanClause.Occur.MUST);
                        booleanQuery.add(queryObjects, BooleanClause.Occur.MUST);

                        //do the search
                        TopDocs hits = searcher.search(parser.parse(String.valueOf(booleanQuery)), 100);
                        //System.out.println("Number of docs hits"+hits.totalHits);
                        for (ScoreDoc scoreDoc : hits.scoreDocs) {
                            Document d = searcher.doc(scoreDoc.doc);

                            if (!eliminatingAllDuplicates.containsKey(wiki_exs.getMentions()+wiki_exs.getObject()) && !eliminatingAllDuplicates.containsValue(d.get(TEXT)))
                            {
                                eliminatingAllDuplicates.put(wiki_exs.getMentions()+wiki_exs.getObject(),d.get(TEXT));
                                fileWriter.append(wiki_exs.getRelation());
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(wiki_exs.getMentions());
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(wiki_exs.getObject());
                                fileWriter.append(COMMA_DELIMITER);
                                String text = d.get(TEXT);
                                text = text.replace("[-+.^:\\/()!']", " ");
                                text = text.replaceAll("[^a-zA-Z0-9]+", " ");
                                //System.out.println(text);
                                fileWriter.append(text);
                                fileWriter.append(NEW_LINE_SEPARATOR);
                                //System.out.println((k + 1) + ". " + d.get(TEXT));
                            }
                        }

                    }
                    System.out.println("Writing Text File for Successful " + concat_filename);
                }

            }
           /* //Print the extracts list
            for (Nell_Extracts exs : extracts) {
                System.out.println(exs.toString());
            }*/

        } catch (Exception e) {
            System.out.println("Error in CsvFileReader/Writer !!!");
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/Writer !!!");
                e.printStackTrace();
            }
        }

        reader.close();
    }


    public static void main(String args[]) throws IOException, ParseException {


        // this has the path where the index is present
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Spring2016/INDStudy/RA/Wikipedia_Index/foundedBy/");

        // this is the path from which the documents to be queried
        File datadirectory = new File("C:/Users/Niranjan/Documents/Spring2016/INDStudy/RA/Freebase_Entities/Query/");

        // this is the path from which the result needs to be stored
        String resultdirectory = "C:/Users/Niranjan/Documents/Spring2016/INDStudy/RA/ResultDirectory/";

        // filetype that is present in the corpus
        String filetype = "txt";

        // this object will call the index method to generate the indexing
        LuceneQueryWikipedia corpusindex = new LuceneQueryWikipedia();

        corpusindex.search_query(indexdirectory, datadirectory, resultdirectory, filetype);

    }

}