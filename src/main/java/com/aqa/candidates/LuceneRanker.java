package com.aqa.candidates;

import com.aqa.kb.KnowledgeBase;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Class that uses the Lucene search engine to answer questions. You will need to implement the {@link
 * #answerQuestion(KnowledgeBase, String)} method.
 * <p/>
 * You should only access the text of the documents (using {@link Document}) from the knowledge base. You will
 * not need to use the information from CoreNLP in the document because Lucene will handle the natural language
 * processing for you.
 * <p/>
 * The following links are useful for finding out how to use Lucene:
 * <p/>
 * <a href="http://oak.cs.ucla.edu/cs144/projects/lucene/">http://oak.cs.ucla.edu/cs144/projects/lucene/</a>
 * <p/>
 * <a href="https://lucene.apache.org/core/6_0_0/core/overview-summary.html">https://lucene.apache
 * .org/core/6_0_0/core/overview-summary.html</a>
 */
public class LuceneRanker implements Ranker {
    private static final String ID_FIELD = "id";
    private static final String TEXT_FIELD = "text";
    private static final int MAX_RESULTS = 10;

    @Override
    public RankedCandidates answerQuestion(KnowledgeBase knowledgeBase, String question) throws IOException, ParseException {

        // Use RAMDirectory from org.apache.lucene.store to create the index in memory
        Directory directory = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // Add docs from knowledge base
        for (com.aqa.kb.Document doc : knowledgeBase.getDocuments()) {
            Document d = new Document();
            d.add(new Field("fieldname", doc.getText(), TextField.TYPE_STORED));
            indexWriter.addDocument(d);
        }
        indexWriter.close();

        // Now search the index
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser parser = new QueryParser("fieldname", analyzer);
        Query query = parser.parse(question);

        ScoreDoc[] hits = indexSearcher.search(query, 1000).scoreDocs;

        RankedCandidates.Builder rankedCandidatesBuilder = new RankedCandidates.Builder(question);

        // Iterate through the results and add them to the builder object
        for (ScoreDoc hit : hits) {
            rankedCandidatesBuilder.addCandidate(knowledgeBase.getDocument(hit.doc + 1), hit.score);
        }

        directoryReader.close();
        directory.close();

        return rankedCandidatesBuilder.build();
    }

}
