package com.example.rollingball.arena;

import com.example.rollingball.Main;
import javafx.scene.shape.Cylinder;
import javafx.geometry.Bounds;
import com.example.rollingball.Utilities;
import javafx.scene.shape.Box;
import javafx.scene.paint.Material;
import javafx.geometry.Point3D;
import javafx.scene.transform.Translate;
import javafx.scene.shape.Sphere;

public class Ball extends Sphere
{
	private Translate position;
	private Point3D speed;

	public Ball( double radius,  Material material,  Translate position) {
		super(radius);
		super.setMaterial(material);
		this.position = position;
		super.getTransforms().add(this.position);
		this.speed = new Point3D(0.0, 0.0, 0.0);
	}


	public boolean update( double ds,  double top,  double bottom,  double left,  double right,  double xAngle,  double zAngle,  double maxAngleOffset,  double maxAcceleration,  double damp) {
		double newPositionX = this.position.getX() + this.speed.getX() * ds;
		double newPositionZ = this.position.getZ() + this.speed.getZ() * ds;
		this.position.setX(newPositionX);
		this.position.setZ(newPositionZ);
		double accelerationX = Main.getSpeed() * zAngle / maxAngleOffset;
		double accelerationZ = -Main.getSpeed() * xAngle / maxAngleOffset;
		double newSpeedX = (this.speed.getX() + accelerationX * ds) * damp; //ovde dodati dodatno snazno odbijanje?
		double newSpeedZ = (this.speed.getZ() + accelerationZ * ds) * damp;
		this.speed = new Point3D(newSpeedX, 0.0, newSpeedZ);
		boolean xOutOfBounds = newPositionX > right || newPositionX < left;
		boolean zOutOfBounds = newPositionZ > top || newPositionZ < bottom;
		return xOutOfBounds || zOutOfBounds;
	}

	public void handleWallCollision( Box wall) {;
		double ballCenterX = this.getBoundsInParent().getCenterX();
		double ballCenterZ = this.getBoundsInParent().getCenterZ();
		double closestX = Utilities.clamp(ballCenterX, wall.getBoundsInParent().getMinX(), wall.getBoundsInParent().getMaxX());
		double closestZ = Utilities.clamp(ballCenterZ, wall.getBoundsInParent().getMinZ(), wall.getBoundsInParent().getMaxZ());
		double dx = closestX - ballCenterX;
		double dz = closestZ - ballCenterZ;
		double distanceSquared = dx * dx + dz * dz;
		double radiusSquared = this.getRadius() * this.getRadius();
		if (distanceSquared < radiusSquared) { //doslo do sudara
			if (closestX == wall.getBoundsInParent().getMinX() || closestX == wall.getBoundsInParent().getMaxX()) {
				this.speed = new Point3D(-this.speed.getX(), 0.0, this.speed.getZ());
			}
			else if (closestZ == wall.getBoundsInParent().getMinZ() || closestZ == wall.getBoundsInParent().getMaxZ()) {
				this.speed = new Point3D(this.speed.getX(), 0.0, -this.speed.getZ());
			}
		}
	}

	public void resetSpeed() {
		this.speed = new Point3D(0.0, 0.0, 0.0);
	}

	public void handleObstacleCollision( Cylinder obstacle) {
		double ballX = this.getBoundsInParent().getCenterX();
		double ballZ = this.getBoundsInParent().getCenterZ();
		double obstacleX = obstacle.getBoundsInParent().getCenterX();
		double obstacleZ = obstacle.getBoundsInParent().getCenterZ();
		double dx = ballX - obstacleX;
		double dz = ballZ - obstacleZ;
		double dr = this.getRadius() + obstacle.getRadius();

		double distanceSquared = dx * dx + dz * dz; //formula za razdaljinu tacaka koren -> (x1-x2)^2 + (y1-y2)^2
		double radiusSquared = dr * dr;

		int factor=2;
		if(obstacle==Main.getSpecial())factor=6; //za specijalnu prepreku posebno odbijanje

		if (distanceSquared < radiusSquared) {//provera doslo do sudara
			Point3D normal = new Point3D(dx, 0, dz).normalize();
			double speedDotNormal = this.speed.dotProduct(normal);
			this.speed = this.speed.subtract(normal.multiply(factor * speedDotNormal));
		}
	}
}
