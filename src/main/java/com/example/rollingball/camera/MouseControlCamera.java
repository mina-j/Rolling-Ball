

package com.example.rollingball.camera;

import javafx.scene.input.ScrollEvent;
import com.example.rollingball.Utilities;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.PerspectiveCamera;

public class MouseControlCamera extends PerspectiveCamera
{
    private Translate position;
    private Rotate rotateX;
    private Rotate rotateY;
    private double previousX;
    private double previousY;

    public MouseControlCamera( Translate position) {
        super(true);
        this.position = position;
        this.rotateX = new Rotate(-45, Rotate.X_AXIS);
        this.rotateY = new Rotate(0, Rotate.Y_AXIS);
        super.getTransforms().addAll(this.rotateY, this.rotateX, this.position);
    }

    public void handleScrollEvent( ScrollEvent event) {
        if (event.getDeltaY() > 0) {
            this.position.setZ(this.position.getZ() + 40);//30
        }
        else {
            this.position.setZ(this.position.getZ() - 40);
        }
    }

    public void handleMouseEvent( MouseEvent event) {
        if (event.getEventType()==MouseEvent.MOUSE_PRESSED) {
            this.previousX = event.getSceneX();
            this.previousY = event.getSceneY();
        }
        else if (event.getEventType()==MouseEvent.MOUSE_DRAGGED) {
            int p1,p2;
            if(event.getSceneX() - this.previousX>0){//dx
                p1=1;
            }else {p1=-1;}

            if(event.getSceneY() - this.previousY>0){//dy
                p2=1;
            }else {p2=-1;}
            this.previousX = event.getSceneX();
            this.previousY = event.getSceneY();

            double newAngleX = this.rotateX.getAngle() - p2 * 1.1;
            double newAngleY = this.rotateY.getAngle() - p1 * 1.1;
            this.rotateX.setAngle(Utilities.clamp(newAngleX, -90.0, 0.0));
            this.rotateY.setAngle(newAngleY);
        }
    }


}
