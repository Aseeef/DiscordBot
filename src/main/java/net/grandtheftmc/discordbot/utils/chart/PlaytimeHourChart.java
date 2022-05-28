package net.grandtheftmc.discordbot.utils.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

// todo: display most frequent times player gets online on
public class PlaytimeHourChart {

    String title;
    Color color;
    int width, height;

    public PlaytimeHourChart(String title, Color color, int width, int height) {
        this.title = title;
        this.color = color;
        this.width = width;
        this.height = height;
    }

    public InputStream get(HashMap<String, Integer> hourMap) throws IOException {

        XYZDataset dataset = createDataset(hourMap);
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(color);
        chart.setTitle(title);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(chart.createBufferedImage(width, height), "png", os);                          // Passing: ​(RenderedImage im, String formatName, OutputStream output)
        return new ByteArrayInputStream(os.toByteArray());
    }

    private static JFreeChart createChart(XYZDataset xyzdataset) {
        //ChartFactory.createTimeSeriesChart()
        JFreeChart jfreechart = ChartFactory.createBubbleChart(
                "AGE vs WEIGHT vs WORK",
                "Weight",
                "AGE",
                xyzdataset,
                PlotOrientation.HORIZONTAL,
                true, true, false);

        XYPlot xyplot = ( XYPlot )jfreechart.getPlot( );
        xyplot.setForegroundAlpha( 0.65F );
        XYItemRenderer xyitemrenderer = xyplot.getRenderer( );
        xyitemrenderer.setSeriesPaint( 0 , Color.blue );
        NumberAxis numberaxis = (NumberAxis)xyplot.getDomainAxis( );
        numberaxis.setLowerMargin( 0.2 );
        numberaxis.setUpperMargin( 0.5 );
        NumberAxis numberaxis1 = ( NumberAxis )xyplot.getRangeAxis( );
        numberaxis1.setLowerMargin( 0.8 );
        numberaxis1.setUpperMargin( 0.9 );

        return jfreechart;
    }

    public static XYZDataset createDataset(HashMap<String, Integer> hourMap) {
        DefaultXYZDataset defaultxyzdataset = new DefaultXYZDataset();
        CategoryTableXYDataset xyDataset = new CategoryTableXYDataset();
        double[] ad = { 30 , 40 , 50 , 60 , 70 , 80 };
        double[] ad1 = { 10 , 20 , 30 , 40 , 50 , 60 };
        double[] ad2 = { 4 , 5 , 10 , 8 , 9 , 6 };
        double[][] ad3 = { ad , ad1 , ad2 };
        defaultxyzdataset.addSeries( "Series 1" , ad3 );

        return defaultxyzdataset;
    }

}
