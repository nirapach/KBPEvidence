package com.KBP.Evidence.Index; /**
 * Created by Niranjan on 12/6/2015.
 */

import edu.stanford.nlp.ling.Sentence;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.DocumentPreprocessor;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("unchecked")
public class LuceneIndexerWikipedia {

    /**
     * @param args
     */
    private static String DOCNO = "DOC";
    private static String TEXT = "extract";

    private void index(File indexdirectory, File datadirectory, String filetype)
            throws Exception {

        Path path = Paths.get(String.valueOf(indexdirectory));
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

        System.out.println("com.KBP.Evidence.Index Commited");
        indwriter.close();
        System.out.println("com.KBP.Evidence.Index Closed");

    }

    private void indexing(IndexWriter indwriter, File datadirectory,
                          String filetype) throws Exception {
        File[] files = datadirectory.listFiles();
        // Document indexDoc = new Document();
        int indexstatus = 0;
        ArrayList<String> tag = new ArrayList<String>();
        tag.add(TEXT);
        //in future if want to index more fields add them here
        //System.out.println(tag.size());
        int corpuslen = 0;
        for (int i = 0; i < files.length; i++) {
            corpuslen++;
            int doc_count = 0;
            int extractCount = 0;
            if (!files[i].isDirectory() && !files[i].isHidden()
                    && files[i].canRead() && files[i].exists()) {
                System.out.println("\n Indexing is going on with file"
                        + files[i].getCanonicalPath());
                // File f = new File(files[i].getCanonicalPath());
                String fileContent = new String(Files.readAllBytes(Paths
                        .get(files[i].getCanonicalPath())));
                String[] alldocs = fileContent.split("</extract>");
                int docsize = alldocs.length;
                String[] documents = new String[docsize - 1];
                for (int k = 0; k < docsize - 1; k++) {
                    documents[k] = alldocs[k];
                }
                for (String docContent : documents) {
                    extractCount += 1;
                    //docContent = docContent.replaceAll("[-+^:\\/()!'=]", " ");
                    System.out.println("Number of extract docs so far:" + extractCount);
                    Reader reader = new StringReader(docContent);
                    DocumentPreprocessor docParser = new DocumentPreprocessor(reader);
                    List<String> sentenceList = new ArrayList<String>();

                    for (List<HasWord> sentence : docParser) {
                        String sentenceString = Sentence.listToString(sentence);
                        sentenceList.add(sentenceString.toString());
                    }

                    for (String posContent : sentenceList) {

                        if (posContent != null && posContent != "\n") {
                            Document document = new Document();
                            doc_count += 1;
                            //System.out.println("Number of NLP parsed docs so far:" + doc_count);

                            String tagContent = "";
                            int startIndex = 0;
                            StringBuffer contentBuffer = new StringBuffer();

                            if (posContent.contains("<" + TEXT + ">")) {
                                String[] inputSplits = posContent.split("<" + TEXT + ">");
                                if (inputSplits.length > 1 && inputSplits[1]!=null) {
                                    contentBuffer.append(inputSplits[1]);
                                    tagContent = contentBuffer.toString();
                                    document.add(new TextField(TEXT, tagContent, Field.Store.YES));
                                }

                            }else{
                                contentBuffer.append(posContent);
                                tagContent = contentBuffer.toString();
                                document.add(new TextField(TEXT, tagContent, Field.Store.YES));
                            }
                            //System.out.println("Adding document");
                            indwriter.addDocument(document);



                        }
                    }
                }


                indexstatus = 1;
            } else {
                indexstatus = 0;
            }
        }

        if (indexstatus == 1) {
            System.out.println("Indexing Successful");
            System.out.println("Total Number of  files in the given corpus"
                    + corpuslen);

        }

    }

    // com.KBP.Evidence.main method where the object for the GenerateIndex class is instantiated
    public static void main(String[] args) throws Exception {

        // this has the path where the index needs to be created
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Spring2016/INDStudy/RA/Wikipedia_Index/headquarters/");

        // this is the path from which the documents to be indexed
        File datadirectory = new File("C:/Users/Niranjan/Documents/Spring2016/INDStudy/RA/Wikipedia_Text/headquarters/");

        // filetype that is present in the corpus
        String filetype = "txt";

        // this object will call the index method to generate the indexing
        LuceneIndexerWikipedia corpusindex = new    LuceneIndexerWikipedia();

        corpusindex.index(indexdirectory, datadirectory, filetype);

    }

}

