import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;

public class Driver {

    private static JMapViewer mapViewer;
    private static Image raccoonImage;

    public static void main(String[] args) throws IOException {
        System.setProperty("http.agent", "MyApp/1.0");
        TripPoint.readFile("triplog.csv");
        ArrayList<TripPoint> trip = TripPoint.getTrip();
        raccoonImage = new ImageIcon("raccoon.png").getImage();

        JFrame frame = new JFrame("Project 5 - Your Name");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel();

        JComboBox<Integer> comboBox = new JComboBox<>(new Integer[]{15, 30, 60, 90});
        panel.add(new JLabel("Animation Time:"));
        panel.add(comboBox);

        JCheckBox checkBox = new JCheckBox("Include Stops");
        checkBox.setSelected(false);
        panel.add(checkBox);

        JButton playButton = new JButton("Play");
        panel.add(playButton);

        frame.add(panel, BorderLayout.NORTH);

        mapViewer = new JMapViewer();
        mapViewer.setTileSource(new OsmTileSource.Mapnik());
        Coordinate center = new Coordinate(trip.get(0).getLat(), trip.get(0).getLon());
        mapViewer.setDisplayPosition(center, 12);
        frame.add(mapViewer, BorderLayout.CENTER);

        playButton.addActionListener(e -> {
            int animationTime = (int) comboBox.getSelectedItem();
            boolean includeStops = checkBox.isSelected();
            try {
                startAnimation(trip, animationTime, includeStops);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        frame.setVisible(true);
    }

    private static void startAnimation(ArrayList<TripPoint> trip, int animationTime, boolean includeStops) throws IOException {
        if (!includeStops) {
            TripPoint.h1StopDetection();
        }
        ArrayList<TripPoint> path = includeStops ? TripPoint.getTrip() : TripPoint.getMovingTrip();

        mapViewer.removeAllMapMarkers();
        mapViewer.removeAllMapPolygons();

        Timer timer = new Timer(animationTime * 1000 / path.size(), null);
        final int[] index = {0};

        final IconMarker[] currentMarker = {null};

        timer.addActionListener(e -> {
            if (index[0] < path.size() - 1) {
                if (currentMarker[0] != null) {
                    mapViewer.removeMapMarker(currentMarker[0]);
                }

                currentMarker[0] = new IconMarker(
                        new Coordinate(path.get(index[0]).getLat(), path.get(index[0]).getLon()), raccoonImage);

                mapViewer.addMapMarker(currentMarker[0]);

                mapViewer.addMapPolygon(new org.openstreetmap.gui.jmapviewer.MapPolygonImpl(
                        new Coordinate(path.get(index[0]).getLat(), path.get(index[0]).getLon()),
                        new Coordinate(path.get(index[0] + 1).getLat(), path.get(index[0] + 1).getLon()),
                        new Coordinate(path.get(index[0] + 1).getLat(), path.get(index[0] + 1).getLon())));

                index[0]++;
            } else {
                ((Timer) e.getSource()).stop();
            }
        });
        timer.start();
    }
}

