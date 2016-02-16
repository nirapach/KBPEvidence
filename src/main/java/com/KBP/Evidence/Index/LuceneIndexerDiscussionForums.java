package com.KBP.Evidence.Index;

/**
 * Created by Niranjan on 12/6/2015.
 */

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

public class LuceneIndexerDiscussionForums {

    /**
     * @param args
     */
    //private static String DOCNO = "doc";
    private static String TEXT = "post";
    private static String HEADLINE = "headline";

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
        int indexstatus = 0;
        ArrayList<String> tag = new ArrayList<String>();
        //tag.add(DOCNO);
        tag.add(TEXT);
        int corpuslen = 0;
        for (int i = 0; i < files.length; i++) {
            corpuslen++;
            if (!files[i].isDirectory() && !files[i].isHidden()
                    && files[i].canRead() && files[i].exists()) {
                System.out.println("\n Indexing is going on with file" + files[i].getCanonicalPath());
                String fileContent = new String(Files.readAllBytes(Paths.get(files[i].getCanonicalPath())));
                String[] alldocs = fileContent.split("</doc>");
                //since the last split will be space subtracting one from the doc count in a single file
                int docsize = alldocs.length;
                String[] documents = new String[docsize - 1];
                for (int k = 0; k < docsize - 1; k++) {
                    documents[k] = alldocs[k];
                }
                for (String docContent : documents) {

                    //this is for the headline tag index since it is present only once per document
                    Document head_document = new Document();

                    String tagContentHead = "";
                    int startIndexHead = 0;
                    StringBuffer contentBufferHead = new StringBuffer();

                    while ((startIndexHead = docContent.indexOf
                            ("<" + HEADLINE + ">", startIndexHead)) != -1) {
                        startIndexHead += HEADLINE.length() + 2;
                        int endindexHead = docContent.indexOf("</" + HEADLINE + ">", startIndexHead);
                        String contentHead = docContent.substring(startIndexHead,endindexHead);
                        contentBufferHead.append(contentHead);
                        startIndexHead += contentHead.length();
                        //System.out.println(content);
                    }
                    tagContentHead = contentBufferHead.toString();
                    head_document.add(new TextField(HEADLINE, tagContentHead,Field.Store.YES));
                    indwriter.addDocument(head_document);
                    //this is for the post tag index in every document
                    String[] posCont = docContent.split("</post>");
                    String[] posContents = new String[posCont.length - 1];
                    for (int p = 0; p < posCont.length - 1; p++) {
                        posContents[p] = posCont[p];
                    }

                    for (String posContent : posContents) {
                        Document document = new Document();
                        if (posContent != null) {
                            for (int j = 0; j < tag.size(); j++) {
                                String tagContent = "";
                                int startIndex = 0;
                                StringBuffer contentBuffer = new StringBuffer();
                                if (j == 0) {
                                    while ((startIndex = posContent.indexOf(
                                            "\n", startIndex)) != -1) {
                                        startIndex += posContent.indexOf("\n") + 1;
                                        int endindex = posContent.length();
                                        //System.out.println(startIndex+","+endindex);
                                        if (endindex > startIndex) {
                                            String content_post = posContent.substring(startIndex, endindex);
                                            int id_index = content_post.indexOf("id=");
                                            String content = content_post.substring(id_index + 9, content_post.length());
                                            //System.out.println(content);
                                            contentBuffer.append(content);
                                            startIndex += content_post.length();
                                        }
                                    }

                                }
                                tagContent = contentBuffer.toString();
                                document.add(new TextField(tag.get(j), tagContent,
                                        Field.Store.YES));
                            }
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
        File indexdirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_index2/");

        // this is the path from which the documents to be indexed
        File datadirectory = new File("C:/Users/Niranjan/Documents/Fall 2015/Independent Study/KBP/discussion_forums2/");

        // filetype that is present in the corpus
        String filetype = "FILE";

        // this object will call the index method to generate the indexing
        LuceneIndexerDiscussionForums corpusindex = new LuceneIndexerDiscussionForums();

        corpusindex.index(indexdirectory, datadirectory, filetype);

    }

}


