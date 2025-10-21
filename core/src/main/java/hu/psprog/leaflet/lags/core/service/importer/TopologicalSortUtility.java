package hu.psprog.leaflet.lags.core.service.importer;

import hu.psprog.leaflet.lags.core.domain.config.OAuthClient;
import hu.psprog.leaflet.lags.core.domain.config.OAuthClientAllowRelation;
import hu.psprog.leaflet.lags.core.domain.config.OAuthConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Utility class to determine the topological order of registered OAuth applications via the legacy configuration API.
 *
 * @author Peter Smith
 */
@Component
class TopologicalSortUtility {

    private final OAuthConfigurationProperties oAuthConfigurationProperties;

    @Autowired
    TopologicalSortUtility(OAuthConfigurationProperties oAuthConfigurationProperties) {
        this.oAuthConfigurationProperties = oAuthConfigurationProperties;
    }

    /**
     * Collects the name of the OAuth applications in topological order based on their defined client relations.
     * Following the returned order ensures that no registration will reference (via client relation) any other that
     * does not exist in the database yet.
     *
     * @return name of the application registrations in topological order
     */
    List<String> topologicalSortClients() {

        Map<String, List<String>> dependencyGraph = oAuthConfigurationProperties.getClients()
                .stream()
                .collect(Collectors.toMap(OAuthClient::getClientName, this::extractAllowedClients));

        return topologicalSort(dependencyGraph);
    }

    private List<String> extractAllowedClients(OAuthClient client) {

        return client.getAllowedClients()
                .stream()
                .map(OAuthClientAllowRelation::getName)
                .toList();
    }

    private List<String> topologicalSort(Map<String, List<String>> graph) {

        Set<String> visited = new HashSet<>();
        Stack<String> stack = new Stack<>();

        for (String node : graph.keySet()) {
            if (!visited.contains(node)) {
                depthFirstSearch(node, graph, visited, stack);
            }
        }

        return stack.stream().toList();
    }

    private void depthFirstSearch(String node, Map<String, List<String>> graph, Set<String> visited, Stack<String> stack) {

        visited.add(node);

        for (String neighbor : graph.getOrDefault(node, new LinkedList<>())) {
            if (!visited.contains(neighbor)) {
                depthFirstSearch(neighbor, graph, visited, stack);
            }
        }

        stack.push(node);
    }
}
