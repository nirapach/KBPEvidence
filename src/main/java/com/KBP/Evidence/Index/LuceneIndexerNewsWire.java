package com.KBP.Evidence.Index; /**
 * Created by Niranjan on 12/6/2015.
 */

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.process.DocumentPreprocessor;
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

public class LuceneIndexerNewsWire {

    /**
     * @param args
     */
    private static String DOCNO = "DOC";
    private static String TEXT = "TEXT";

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

        indwriter.close();

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
            if (!files[i].isDirectory() && !files[i].isHidden()
                    && files[i].canRead() && files[i].exists()) {
                System.out.println("\n Indexing is going on with file"
                        + files[i].getCanonicalPath());
                // File f = new File(files[i].getCanonicalPath());
                String fileContent = new String(Files.readAllBytes(Paths
                        .get(files[i].getCanonicalPath())));
                String[] alldocs = fileContent.split("</DOC>");
                int docsize = alldocs.length;
                String[] documents = new String[docsize - 1];
                for (int k = 0; k < docsize - 1; k++) {
                    documents[k] = alldocs[k];
                }
                for (String docContent : documents) {
                    String[] posContentsList = docContent.split("</P>");
                    String para = "P";
                    for (String posContent : posContentsList) {

                        Reader reader = new StringReader(posContent);
                        DocumentPreprocessor docParser = new DocumentPreprocessor(reader);
                        List<String> sentenceList = new ArrayList<String>();

                        for (List<HasWord> sentence : docParser) {
                            String sentenceString = Sentence.listToString(sentence);
                            sentenceList.add(sentenceString.toString());
                        }

                        for (String indexContent : sentenceList) {

                            if (posContent != null) {
                                doc_count += 1;
                                Document document = new Document();
                                for (int j = 0; j < tag.size(); j++) {
                                    String tagContent = "";
                                    int startIndex = 0;
                                    StringBuffer contentBuffer = new StringBuffer();
                                    if (j == 0) {
                                 /*   while ((startIndex = docContent.indexOf
                                            ("<" + tag.get(j), startIndex)) != -1) {

                                        startIndex += tag.get(j).length() + 6;
                                        int endindex = docContent.indexOf("\" type", startIndex);
                                        String content = docContent.substring(startIndex,
                                                endindex);
                                        contentBuffer.append(content);
                                        startIndex += content.length();
                                        // System.out.println(content);
                                    }*/
                                        while ((startIndex = docContent.indexOf(
                                                "<" + para + ">", startIndex)) != -1) {

                                            startIndex += para.length() + 3;
                                            // int endindex = docContent.indexOf("</" + tag.get(j)
                                            //    + ">", startIndex);
                                            int endindex = posContent.length();
                                            if (endindex > startIndex) {
                                                String content = docContent.substring(startIndex,
                                                        endindex);
                                                contentBuffer.append(content);
                                                startIndex += content.length();
                                            }
                                            // System.out.println(content);
                                        }

                                    } /*else if (j == 1) {
                                    *//*while ((startIndex = docContent.indexOf(
                                            "<" + para + ">", startIndex)) != -1) {

                                        startIndex += para.length() + 3;
                                        // int endindex = docContent.indexOf("</" + tag.get(j)
                                        //    + ">", startIndex);
                                        int endindex = posContent.length();
                                        if (endindex > startIndex) {
                                            String content = docContent.substring(startIndex,
                                                    endindex);
                                            contentBuffer.append(content);
                                            startIndex += content.length();
                                        }
                                        // System.out.println(content);
                                    }*//*
                                }*/

                                    tagContent = contentBuffer.toString();
                                    // System.out.println(tagContent);
                                    if (j == 0)
                                        //document.add(new StringField(DOCNO, tagContent, Field.Store.YES));
                                        document.add(new StringField(DOCNO, "doc_no_" + doc_count, Field.Store.YES));
                                        document.add(new TextField(tag.get(j), tagContent, Field.Store.YES));
                               /* else
                                    //System.out.println(tag.get(j));
                                    document.add(new TextField(tag.get(j), tagContent, Field.Store.YES));*/
                                }
                                //System.out.println("Adding document");
                                indwriter.addDocument(document);

                            }
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
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/NewsWire_Index/");

        // this is the path from which the documents to be indexed
        File datadirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/newswire/");

        // filetype that is present in the corpus
        String filetype = "FILE";

        // this object will call the index method to generate the indexing
        LuceneIndexerNewsWire corpusindex = new LuceneIndexerNewsWire();

        corpusindex.index(indexdirectory, datadirectory, filetype);

    }

}

