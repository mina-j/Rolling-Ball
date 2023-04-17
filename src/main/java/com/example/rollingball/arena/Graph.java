

package com.example.rollingball.arena;

import javafx.scene.shape.Line;
import javafx.scene.transform.Translate;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;

public class Graph extends Group
{
    private Line line;
    private double width;
    private double height;

    public Graph() {
        this.width=150;
        this.height=150;
        Rectangle box = new Rectangle(this.width, this.height, Color.GREEN);
        box.setStyle("    -fx-stroke: red; -fx-stroke-width: 4;");
        box.getTransforms().add(new Translate(-this.width/2, -this.height/2));
        this.getChildren().add(box);

        line = new Line(0,0,0,0);
        line.setStyle("    -fx-stroke: black; -fx-stroke-width: 3;");
        line.setFill(Color.RED);
        this.line.getTransforms().addAll( new Translate(0.0, 0));
        this.getChildren().add(this.line);
    }

    public void update( double xAngle,  double zAngle,  double maxAngleOffset) {
        this.line.setEndY(width*(xAngle/maxAngleOffset)/2);
        this.line.setEndX(width*(zAngle/maxAngleOffset)/2);
    }
}
