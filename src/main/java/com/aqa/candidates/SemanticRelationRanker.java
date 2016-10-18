package com.aqa.candidates;

import com.aqa.kb.Document;
import com.aqa.kb.KnowledgeBase;
import com.aqa.relations.SemanticRelation;
import com.aqa.relations.SemanticRelationExtractor;
import com.aqa.relations.SimpleSemanticRelationExtractor;
import edu.stanford.nlp.simple.Sentence;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The class that uses a {@link SemanticRelationExtractor} to answer questions. You will need to implement the {@link
 * #answerQuestion(KnowledgeBase, String, SemanticRelationExtractor)} method.
 */
public class SemanticRelationRanker implements Ranker {
    @Override
    public RankedCandidates answerQuestion(KnowledgeBase knowledgeBase, String question) {
        return answerQuestion(knowledgeBase, question, SimpleSemanticRelationExtractor.getSemanticRelationExtractor());
    }

    private RankedCandidates answerQuestion(KnowledgeBase knowledgeBase, String question,
                                            SemanticRelationExtractor semanticRelationExtractor) {

        // get SemanticRelation data for question
        List<SemanticRelation> questionSemanticRelation = semanticRelationExtractor.extractSemanticRelations(new Sentence(question));
        Map<String, String> questionSemanticRelationFeatures = questionSemanticRelation.get(0).getFeatures();

        SortedSet<RankedCandidate> rankedCandidates = new TreeSet<>();

        // compare against semantic relation in documents and rate based on match
        for (Document employment : knowledgeBase.getDocuments()) {
            List<SemanticRelation> semanticRelationList = employment.getSemanticRelations();

            if (semanticRelationList.size() > 0) {
                SemanticRelation semanticRelation = semanticRelationList.get(0);
                Map<String, String> employmentSemanticRelationFeatures = semanticRelation.getFeatures();

                if (questionSemanticRelationFeatures.get("employee") == null && semanticRelation.getFeatures().get("employee") != null) {
                    if (questionSemanticRelationFeatures.get("employer").equals(employmentSemanticRelationFeatures.get("employer"))
                            && questionSemanticRelationFeatures.get("location").equals(employmentSemanticRelationFeatures.get("location")))
                        rankedCandidates.add(new RankedCandidate(1, employment));
                }

            }
        }

        return new RankedCandidates.Builder(question).addCandidates(rankedCandidates).build();
    }
}
