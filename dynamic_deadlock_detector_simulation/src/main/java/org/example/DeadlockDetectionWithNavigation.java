package org.example;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.util.HashSet;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DeadlockDetectionWithNavigation {

    private static int currentIndex = 0;
    private static JFrame frame;
    private static JPanel graphPanel;
    private static JTextArea textArea;

    public static void initializeGUI() {
        SwingUtilities.invokeLater(DeadlockDetectionWithNavigation::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Wait-For Graph Navigation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        graphPanel = new JPanel(new BorderLayout());
        textArea = new JTextArea();
        textArea.setEditable(false);

        splitPane.setLeftComponent(new JScrollPane(graphPanel));
        splitPane.setRightComponent(new JScrollPane(textArea));
        splitPane.setDividerLocation(600);

        JButton prevButton = new JButton("< Previous");
        JButton nextButton = new JButton("Next >");

        prevButton.addActionListener(e -> showGraph(--currentIndex));
        nextButton.addActionListener(e -> showGraph(++currentIndex));

        JPanel controlPanel = new JPanel();
        controlPanel.add(prevButton);
        controlPanel.add(nextButton);

        frame.add(splitPane, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);
        frame.setVisible(true);

        // Initial graph display
        showGraph(currentIndex);
    }

    private static void showGraph(int index) {
        graphPanel.removeAll();
        synchronized (LogProcessor.getGraphInfoList()) {
            if (index < 0 || index >= LogProcessor.getGraphInfoList().size()) {
                index = Math.max(0, Math.min(index, LogProcessor.getGraphInfoList().size() - 1));
                currentIndex = index;
                return;
            }
            GraphInfo graphInfo = LogProcessor.getGraphInfoList().get(index);
            mxGraph mxGraph = new mxGraph();
            Object parent = mxGraph.getDefaultParent();
            mxGraph.getModel().beginUpdate();
            try {
                Map<String, Object> vertexMap = new HashMap<>();
                for (String node : graphInfo.getGraph().keySet()) {
                    vertexMap.put(node, mxGraph.insertVertex(parent, null, node, 0, 0, 80, 30));
                }
                for (Map.Entry<String, Set<String>> entry : graphInfo.getGraph().entrySet()) {
                    String from = entry.getKey();
                    for (String to : entry.getValue()) {
                        if (!vertexMap.containsKey(to)) {
                            vertexMap.put(to, mxGraph.insertVertex(parent, null, to, 0, 0, 80, 30));
                        }
                        mxGraph.insertEdge(parent, null, "", vertexMap.get(from), vertexMap.get(to));
                    }
                }
            } finally {
                mxGraph.getModel().endUpdate();
            }

            mxCircleLayout layout = new mxCircleLayout(mxGraph);
            layout.execute(mxGraph.getDefaultParent());

            mxGraphComponent graphComponent = new mxGraphComponent(mxGraph);
            graphPanel.add(graphComponent, BorderLayout.CENTER);
            graphPanel.revalidate();
            graphPanel.repaint();

            frame.setTitle("Wait-For Graph Navigation - " + (graphInfo.isDeadlock() ? "Deadlock Detected" : "No Deadlock") + " (Graph " + graphInfo.getId() + ")");

            // Display deadlock information in the text area
            if (graphInfo.isDeadlock()) {
                Set<String> deadlockedThreads = getDeadlockedThreads(graphInfo.getGraph());
                StringBuilder sb = new StringBuilder("Deadlock detected involving threads:\n");
                for (String thread : deadlockedThreads) {
                    sb.append(thread).append("\n");
                }
                textArea.setText(sb.toString());
            } else {
                textArea.setText("No deadlocks detected.");
            }
        }
    }

    private static Set<String> getDeadlockedThreads(Map<String, Set<String>> waitForGraph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();
        Set<String> deadlockedThreads = new HashSet<>();

        for (String node : waitForGraph.keySet()) {
            if (detectCycleDFS(node, waitForGraph, visited, recStack)) {
                deadlockedThreads.addAll(recStack);
            }
        }
        return deadlockedThreads;
    }

    private static boolean detectCycleDFS(String current, Map<String, Set<String>> graph, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(current)) {
            return true;
        }

        if (visited.contains(current)) {
            return false;
        }

        visited.add(current);
        recStack.add(current);

        Set<String> neighbors = graph.getOrDefault(current, new HashSet<>());
        for (String neighbor : neighbors) {
            if (detectCycleDFS(neighbor, graph, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(current);
        return false;
    }
}
