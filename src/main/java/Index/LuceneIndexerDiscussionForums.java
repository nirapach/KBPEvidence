package Index; /**
 * Created by Niranjan on 12/6/2015.
 */
/**
 * Created by Niranjan on 12/6/2015.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;

public class LuceneIndexerDiscussionForums {

    /**
     * @param args
     */
    private static String DOCNO = "doc";
    private static String TEXT = "post";

    private void index(File indexdirectory, File datadirectory, String filetype)
            throws Exception {

        Date start = new Date();

        Path path =Paths.get(String.valueOf(indexdirectory));
        Directory inddir;
        inddir = FSDirectory.open(path);
        StandardAnalyzer indexanalyzer = new StandardAnalyzer();

        IndexWriterConfig indwrcon = new IndexWriterConfig(indexanalyzer);

        indwrcon.setOpenMode(OpenMode.CREATE);

        // to write the documents to the index
        IndexWriter indwriter = new IndexWriter(inddir, indwrcon);

        indexing(indwriter, datadirectory, filetype);

        indwriter.forceMerge(1);
        indwriter.commit();

        indwriter.close();
        Date end = new Date();

    }

    private void indexing(IndexWriter indwriter, File datadirectory,
                          String filetype) throws Exception {
        File[] files = datadirectory.listFiles();
        int indexstatus = 0;
        ArrayList<String> tag = new ArrayList<String>();
        tag.add(DOCNO);
        tag.add(TEXT);
        //System.out.println(tag.size());
        int corpuslen = 0;
        for (int i = 0; i < files.length; i++) {
            corpuslen++;
            if (!files[i].isDirectory() && !files[i].isHidden()
                    && files[i].canRead() && files[i].exists()) {
                System.out.println("\n Indexing is going on with file"
                        + files[i].getCanonicalPath());
                // File f = new File(files[i].getCanonicalPath());
                String fileContent = new String(Files.readAllBytes(Paths
                        .get(files[i].getCanonicalPath())));
                String[] alldocs = fileContent.split("</doc>");
                //since the last split will be space subtracting one from the doc count in a single trectext file
                int docsize = alldocs.length;
                String[] documents = new String[docsize-1];
                for(int k=0;k<docsize-1;k++){
                    documents[k]  = alldocs[k];
                }
                for (String docContent : documents) {
                    Document document = new Document();
                    String[] posContents=docContent.split("</post>");
                    for (int j = 0; j < tag.size(); j++) {
                        String tagContent = "";
                        int startIndex = 0;
                        StringBuffer contentBuffer = new StringBuffer();
                        if(j==0){
                            while ((startIndex = docContent.indexOf
                                    ("<" + tag.get(j), startIndex)) != -1) {

                                startIndex += tag.get(j).length() + 6;
                                int endindex = docContent.indexOf("\"" +">", startIndex);
                                String content = docContent.substring(startIndex,
                                        endindex);
                                contentBuffer.append(content);
                                startIndex += content.length();
                            }
                        }
                        else if(j==1) {
                            for (String posContent : posContents){
                                if(posContent!=null) {
                                    while ((startIndex = posContent.indexOf(
                                            "\">", startIndex)) != -1) {
                                        startIndex += tag.get(j).length() + 1;
                                        int endindex = posContent.length();
                                        //System.out.println(startIndex+","+endindex);
                                        if(endindex>startIndex) {
                                            String content = posContent.substring(startIndex, endindex);
                                            //System.out.println(content);
                                            contentBuffer.append(content);
                                            startIndex += content.length();
                                        }
                                    }
                                }
                            }
                        }

                        tagContent = contentBuffer.toString();
                        // System.out.println(tagContent);
                        if (j == 0)
                            document.add(new StringField(DOCNO, tagContent,
                                    Field.Store.YES));
                        else
                            document.add(new TextField(tag.get(j), tagContent,
                                    Field.Store.YES));

                    }
                    //System.out.println("Adding document");
                    indwriter.addDocument(document);
                }

                indexstatus = 1;
            }

            else {
                indexstatus = 0;
            }
        }

        if (indexstatus == 1) {
            System.out.println("Indexing Successful");
            System.out.println("Total Number of  files in the given corpus"
                    + corpuslen);

        }

    }

    // main method where the object for the GenerateIndex class is instantiated
    public static void main(String[] args) throws Exception {

        // this has the path where the index needs to be created
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_index3/");

        // this is the path from which the documents to be indexed
        File datadirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_forums3/");

        // filetype that is present in the corpus
        String filetype = "FILE";

        // this object will call the index method to generate the indexing
        LuceneIndexerDiscussionForums corpusindex = new LuceneIndexerDiscussionForums();

        corpusindex.index(indexdirectory, datadirectory, filetype);

    }

}


