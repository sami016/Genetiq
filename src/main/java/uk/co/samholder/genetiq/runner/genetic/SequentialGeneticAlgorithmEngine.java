/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.co.samholder.genetiq.runner.genetic;

import java.util.List;
import uk.co.samholder.genetiq.data.RunData;
import uk.co.samholder.genetiq.interactor.Interactor;
import uk.co.samholder.genetiq.population.Population;
import uk.co.samholder.genetiq.population.PopulationModel;
import uk.co.samholder.genetiq.round.RoundStrategy;
import uk.co.samholder.genetiq.termination.TerminationCondition;
import uk.co.samholder.genetiq.variation.StandardVariationEngine;
import uk.co.samholder.genetiq.variation.VariationEngine;

/**
 * Simple sequential genetic algorithm engine implementation. 
 *
 * @author Sam Holder
 * @param <I> individual type
 */
public class SequentialGeneticAlgorithmEngine<I> implements GeneticAlgorithmEngine<I> {
    
    @Override
    public RunData executePipeline(GeneticAlgorithmConfiguration<I> pipeline) {
        RoundStrategy<I> roundStrategy = pipeline.roundStrategy();
        PopulationModel<I> populationModel = pipeline.populationModel();
        VariationEngine<I> variationEngine = new StandardVariationEngine<>(pipeline.variationPipeline());
        TerminationCondition<I> terminationCondition = pipeline.terminationCondition();
        List<Interactor> interactors = pipeline.interactors();
        // Create the run data.
        RunData data = new RunData();
        
        data.set(GeneticAlgorithmEngine.KEY_PERIOD_TYPE, roundStrategy.getPeriodType());
        
        // Generate the initial population.
        populationModel.seedIndividuals();
        populationModel.writeData(data);

        int iteration = 0;
        // Run the loop until termination condition is met.
        while (!populationModel.isConditionMet(terminationCondition, iteration)) {
            // Within each of the model's populations, perform the round.
            for (Population<I> population : populationModel) {
                roundStrategy.performRound(population, variationEngine);
            }
            populationModel.writeData(data);
            // Set and increase the iteration.
            iteration++;
            data.set(GeneticAlgorithmEngine.KEY_PERIOD_NUMBER, iteration);
            // Prompt external interactions.
            doInteractions(data, interactors);
        }
        return data;
    }
    

    /**
     * Run all interactors.
     * @param runData run data
     * @param interactors interactions list
     */
    private void doInteractions(RunData runData, List<Interactor> interactors) {
        for (Interactor interactor : interactors) {
            interactor.interact(runData);
        }
    }

}
