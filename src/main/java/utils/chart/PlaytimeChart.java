package utils.chart;

import com.google.common.primitives.Doubles;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.AreaRendererEndType;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PlaytimeChart {

    String title;
    Color color;
    int width, height;

    public PlaytimeChart(String title, Color color, int width, int height) {
        this.title = title;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public InputStream get(List<String> keys, List<Double> totalPlaytime, List<Double> activePlaytime, List<Double> afkPlaytime) throws IOException {

        CategoryDataset dataset = createDataset(keys, totalPlaytime, activePlaytime, afkPlaytime);
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(color);
        chart.setTitle(title);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(chart.createBufferedImage(width, height), "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
        return new ByteArrayInputStream(os.toByteArray());
    }

    private CategoryDataset createDataset(List<String> keys, List<Double> totalPlaytime, List<Double> activePlaytime, List<Double> afkPlaytime) {

        double[] totalPTArray = Doubles.toArray(totalPlaytime);
        double[] activePTArray = Doubles.toArray(activePlaytime);
        double[] afkPTArray = Doubles.toArray(afkPlaytime);
        String[] keysArray = keys.toArray(new String[0]);

        double[][] data = new double[][]{totalPTArray, activePTArray, afkPTArray};

        CategoryDataset dataset = DatasetUtilities.createCategoryDataset(
                new String[]{"Total Playtime", "Active Playtime", "AFK Playtime"}, keysArray, data);

        return dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {

        JFreeChart chart = ChartFactory.createAreaChart(
                "Oil consumption",
                "Date",
                "Hours Played",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                true
        );

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setForegroundAlpha(0.3f);

        AreaRenderer renderer = (AreaRenderer) plot.getRenderer();
        renderer.setEndType(AreaRendererEndType.LEVEL);

        chart.setTitle(new TextTitle("Oil consumption",
                new Font("Serif", java.awt.Font.BOLD, 18))
        );

        return chart;
    }

}
