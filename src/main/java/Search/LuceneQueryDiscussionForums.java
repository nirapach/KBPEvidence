package Search;

import PojoClasses.Nell_Extracts;
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
import java.util.List;


public class LuceneQueryDiscussionForums {

    private static String TEXT = "post";
    private static String HEADLINE = "headline";
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

        QueryParser parser_headline = new QueryParser(HEADLINE, new StandardAnalyzer());
        ArrayList<String> tag = new ArrayList<String>();
        //tag.add(DOCNO);
        tag.add(TEXT);
        tag.add(HEADLINE);

        try {

            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory() && !files[i].isHidden() && files[i].canRead() && files[i].exists()) {


                    //Create the file reader
                    List<Nell_Extracts> extracts = new ArrayList();

                    //System.out.println(concat_filename);
                    fileReader = new BufferedReader(new FileReader(files[i]));
                    String line = "";
                    //Read the CSV file header to skip it
                    fileReader.readLine();

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
                            tokens[object_id] = tokens[mention_id].replaceAll("[-+.^:,\\/()!']", " ");
                            if (tokens[mention_id] != null && tokens[object_id] != null) {
                                Nell_Extracts ne_ex = new Nell_Extracts(tokens[relation_id], tokens[mention_id], tokens[object_id]);
                                extracts.add(ne_ex);
                            }
                        }
                    }
                    for (int j = 0; j < tag.size(); j++) {
                        String old_filename = files[i].getAbsolutePath();
                        int ch = old_filename.lastIndexOf("\\");
                        String concat_filename = old_filename.substring(ch + 1, old_filename.length() - 4);
                        //create File object
                        File file = new File(resultdirectory +tag.get(j)+ "_DiscussionForums_" + concat_filename + "_Evidence" + "." + filetype);
                        String filename = file.getAbsolutePath();
                        //File writer
                        fileWriter = new FileWriter(filename);
                        //Write the CSV file header
                        fileWriter.append(FILE_HEADER.toString());
                        //Add a new line separator after the header
                        fileWriter.append(NEW_LINE_SEPARATOR);

                        System.out.println("Writing CSV File for " + concat_filename);
                        for (Nell_Extracts nel_exs : extracts) {
                            //lucene query

                            TopScoreDocCollector collector_bool = TopScoreDocCollector.create(500);
                            BooleanQuery query = new BooleanQuery();
                            Query query1 = new TermQuery(new Term(TEXT, nel_exs.getMentions()));
                            Query query2 = new TermQuery(new Term(TEXT, nel_exs.getObject()));
                            query.add(query1, BooleanClause.Occur.MUST);
                            query.add(query2, BooleanClause.Occur.MUST);
                            if(j==0) {
                                searcher.search(parser.parse(QueryParser.escape(String.valueOf(query))), collector_bool);
                            }
                            else if(j==1){
                                searcher.search(parser_headline.parse(QueryParser.escape(String.valueOf(query))), collector_bool);
                            }
                            ScoreDoc[] hits_bool = collector_bool.topDocs().scoreDocs;

                            for (int k = 0; k < hits_bool.length; k++) {
                                int docId = hits_bool[k].doc;
                                Document d = searcher.doc(docId);

                                fileWriter.append(nel_exs.getRelation());
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(nel_exs.getMentions());
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(nel_exs.getObject());
                                fileWriter.append(COMMA_DELIMITER);
                                String text = d.get(tag.get(j));
                                text = text.replace(",", ";");
                                fileWriter.append(text);
                                fileWriter.append(COMMA_DELIMITER);
                                fileWriter.append(NEW_LINE_SEPARATOR);
                            }

                        }
                        System.out.println("Writing CSV File for Successful " + concat_filename);
                    }
                }

            }
        }
        catch (Exception e) {
            System.out.println("Error in CsvFileReader/Writer !!!");
            e.printStackTrace();
        }
        finally {
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


        // this has the path where the index needs to be created
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_index2/");

        // this is the path from which the documents to be queried
        File datadirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/nell_relations_extracted/");

        // this is the path from which the result needs to be stored
        String resultdirectory = "C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_result_nell/";

        // filetype that is present in the corpus
        String filetype = "csv";

        // this object will call the index method to generate the indexing
        LuceneQueryDiscussionForums corpusindex = new LuceneQueryDiscussionForums();

        corpusindex.search_query(indexdirectory, datadirectory, resultdirectory, filetype);

    }

}