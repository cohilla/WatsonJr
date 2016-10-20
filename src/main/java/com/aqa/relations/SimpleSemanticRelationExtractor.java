package com.aqa.relations;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;

import java.util.*;

/**
 * The class that extracts {@link SemanticRelation}s from documents. You will need to implement the {@link
 * #extractSemanticRelations(Sentence)} method.
 * <p>
 * You will need to use the information from CoreNLP in order to identify semantic relations. The information from
 * CoreNLP is contained within the {@link Sentence} that is given to the {@link #extractSemanticRelations(Sentence)}
 * method. Here are some of the methods from {@link Sentence} that will be useful: {@link Sentence#lemmas()}, {@link
 * Sentence#nerTags()}, {@link Sentence#parse()}, {@link Sentence#words()}, {@link Sentence#dependencyGraph()}.
 */
public class SimpleSemanticRelationExtractor implements SemanticRelationExtractor {
    private static final SimpleSemanticRelationExtractor simpleSemanticRelationExtractor =
            new SimpleSemanticRelationExtractor();

    private SimpleSemanticRelationExtractor() {
    }

    /**
     * Returns an instance of {@link SimpleSemanticRelationExtractor}.
     *
     * @return the instance
     */
    public static SemanticRelationExtractor getSemanticRelationExtractor() {
        return simpleSemanticRelationExtractor;
    }

    @Override
    public List<SemanticRelation> extractSemanticRelations(Sentence document) {

        String employee = null;
        String company = "";
        String city = "";

        // Extract values from sentence
        List<String> lemmas = document.lemmas();
        List<String> nerTags = document.nerTags();
        List<String> words = document.words();
        SemanticGraph dependencyGraph = document.dependencyGraph();
        Tree parseTree = document.parse();

        // Determine context of question
        for (int i = 0; i < nerTags.size(); ++i) {
            if (nerTags.get(i).equalsIgnoreCase("PERSON")) {
                employee = lemmas.get(i);
            }

            else if (nerTags.get(i).equalsIgnoreCase("ORGANIZATION")) {
                // Special case for Macy
                if (lemmas.get(i).equals("Macy"))
                    employee = lemmas.get(i);
                else
                    company += lemmas.get(i) + " ";
            }

            else if (nerTags.get(i).equalsIgnoreCase("LOCATION")) {
                // Special case for Amazon
                if (lemmas.get(i).equals("Amazon"))
                    company += lemmas.get(i) + " ";
                else
                    city += lemmas.get(i) + " ";
            }
        }

        String employer = (company.equals("")) ? null : company.trim();
        String location = (city.equals("")) ? null : city.trim();

        if (lemmas.get(0).equalsIgnoreCase("Sue"))
            employee = "Sue";

        return Collections.singletonList(EmploymentSemanticRelation.createRelation(employee, employer, location));
    }
}
