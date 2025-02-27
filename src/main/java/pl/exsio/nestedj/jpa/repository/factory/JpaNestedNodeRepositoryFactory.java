package pl.exsio.nestedj.jpa.repository.factory;

import pl.exsio.nestedj.DelegatingNestedNodeRepository;
import pl.exsio.nestedj.NestedNodeRepository;
import pl.exsio.nestedj.config.jpa.JpaNestedNodeRepositoryConfiguration;
import pl.exsio.nestedj.delegate.control.*;
import pl.exsio.nestedj.delegate.query.jpa.*;
import pl.exsio.nestedj.model.NestedNode;

import java.io.Serializable;

public final class JpaNestedNodeRepositoryFactory {

    private JpaNestedNodeRepositoryFactory() {
    }


    public static <ID extends Serializable, N extends NestedNode<ID>> NestedNodeRepository<ID, N> create(JpaNestedNodeRepositoryConfiguration<ID, N> configuration) {
        QueryBasedNestedNodeInserter<ID, N> inserter = new QueryBasedNestedNodeInserter<>(new JpaNestedNodeInsertingQueryDelegate<>(configuration));
        QueryBasedNestedNodeRetriever<ID, N> retriever = new QueryBasedNestedNodeRetriever<>(new JpaNestedNodeRetrievingQueryDelegate<>(configuration));
        return new DelegatingNestedNodeRepository<>(
                new QueryBasedNestedNodeMover<>(new JpaNestedNodeMovingQueryDelegate<>(configuration)),
                new QueryBasedNestedNodeRemover<>(new JpaNestedNodeIRemovingQueryDelegate<>(configuration)),
                retriever,
                new QueryBasedNestedNodeRebuilder<>(inserter, retriever, new JpaNestedNodeRebuildingQueryDelegate<>(configuration)),
                inserter
        );
    }
}
