package fr.inria.atlanmod.neoemf.util;

import fr.inria.atlanmod.commons.annotation.Static;
import fr.inria.atlanmod.commons.log.Log;
import fr.inria.atlanmod.neoemf.util.compare.LazyMatchEngineFactory;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.match.IMatchEngine;
import org.eclipse.emf.compare.match.impl.MatchEngineFactoryRegistryImpl;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.compare.utils.UseIdentifiers;
import org.eclipse.emf.ecore.EObject;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

import static org.assertj.core.api.Assertions.assertThat;

@Static
@ParametersAreNonnullByDefault
public final class ModelComparisonUtils {

    @SuppressWarnings("JavaDoc")
    private ModelComparisonUtils() {
        throw new IllegalStateException("This class should not be instantiated");
    }

    /**
     * Checks that the {@code actual} object is the same as the {@code expected}.
     *
     * @param actual   the object to check
     * @param expected the expected object
     */
    public static void assertEObjectAreEqual(EObject actual, EObject expected) {
        Log.info("Comparing models...");

        IMatchEngine.Factory factory = new LazyMatchEngineFactory(UseIdentifiers.NEVER);

        IMatchEngine.Factory.Registry registry = new MatchEngineFactoryRegistryImpl();
        registry.add(factory);

        IComparisonScope scope = new DefaultComparisonScope(expected, actual, null);

        Comparison comparison = EMFCompare.builder()
                .setMatchEngineFactoryRegistry(registry)
                .build()
                .compare(scope);

        List<Diff> differences = comparison.getDifferences();

        if (differences.isEmpty()) {
            Log.info("Models are equivalent");
        }
        else {
            Log.warn("Models have {0} differences", differences.size());
        }

        assertThat(differences).hasSize(0);
    }
}