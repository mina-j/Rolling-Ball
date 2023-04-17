

package com.example.rollingball;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.KeyCode;
import java.io.IOException;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.Scene;
import com.example.rollingball.timer.Timer;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.animation.KeyValue;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.scene.transform.Rotate;
import javafx.scene.paint.Material;
import javafx.scene.text.Font;
import javafx.scene.paint.Color;
import com.example.rollingball.arena.Graph;
import javafx.scene.PointLight;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Text;
import javafx.scene.transform.Translate;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Box;
import javafx.scene.Camera;
import com.example.rollingball.camera.MouseControlCamera;
import javafx.scene.SubScene;
import com.example.rollingball.arena.Hole;
import com.example.rollingball.arena.Arena;
import com.example.rollingball.arena.Ball;
import javafx.scene.Group;
import javafx.application.Application;

public class Main extends Application
{
	private static final double WINDOW_WIDTH = 800.0;
	private static final double WINDOW_HEIGHT = 800.0;
	private static final double PODIUM_WIDTH = 2000.0;
	private static final double PODIUM_HEIGHT = 10.0;
	private static final double PODIUM_DEPTH = 2000.0;
	private static final double CAMERA_FAR_CLIP = 100000.0;
	private static final double CAMERA_Z = -5000.0;
	private static final double CAMERA_X_ANGLE = -45.0;
	private static final double BALL_RADIUS = 50.0;
	private static final double DAMP = 0.999;
	private static final double MAX_ANGLE_OFFSET = 30.0;
	private static final double HOLE_RADIUS = 100;
	private static final double BIRD_VIEW_CAMERA_Y = -2500.0;
	private static final double WALL_WIDTH = 10.0;
	private static final double WALL_HEIGHT = 100.0;
	private static final int STARTTIME = 100;
	private static final double ORIENTATION_MAP_WIDTH = 160.0;
	private static final double ORIENTATION_MAP_HEIGHT = 160.0;
	private static final double OBSTACLE_RADIUS = 50.0;
	private static final double OBSTACLE_HEIGHT = 200.0;
	private Group root;
	IntegerProperty timeSeconds =new SimpleIntegerProperty((STARTTIME)*100 ); //za time bar
	Label timerLabel=new Label(); //za time bar
	private Material ballMaterial;
	private Ball ball;
	private static double max_shoot_speed=150;
	private Arena arena;
	private int podium_type;
	private boolean started=false;
	private Hole hole;
	private Stage stage=new Stage();
	private SubScene scene;
	private Scene whole_scene;
	private MouseControlCamera main_camera;
	private Camera above_camera;
	private static ArrayList<Box> walls= new ArrayList<>();
	private Group alt_root;
	private ArrayList<Circle> ballsLeftList=new ArrayList<>(); //prikaz preostalih zivota
	private Translate ballPosition;
	private PhongMaterial lampColor;
	private Group lampBox;
	private PointLight light;
	private boolean isLightOn;
	private Graph graph;
	private static ArrayList<Cylinder> pillars= new ArrayList<>();
	private static CopyOnWriteArrayList<Hole> holes= new CopyOnWriteArrayList<>();
	private static CopyOnWriteArrayList<Cylinder> tokens= new CopyOnWriteArrayList<>();
	private Text loptetext=new Text(),poenitext=new Text(); //ispis poena i zivota
	private int score=0;
	private int ballsLeft = 5;
	private ProgressBar bar;
	private Timeline task;
	private
	BorderPane border=new BorderPane();

	private static Cylinder special_pillar;

	public static Cylinder getSpecial(){
		return special_pillar;
	}

	private void initStatsScene() {
		for(int i = 0; i < 5; i++){
			Circle c=new Circle(WINDOW_WIDTH-80 + i*15, 10, 5,Color.RED);
			c.setStyle("    -fx-stroke: black; -fx-stroke-width: 1;");
			this.root.getChildren().add(c);
			ballsLeftList.add(c);
		}
		this.alt_root.getChildren().addAll(this.ballsLeftList);
		poenitext.setText("Broj poena: "+score);
		poenitext.setX(10);
		poenitext.setY(15);
		poenitext.setFont(Font.font ("Times New Roman", FontWeight.BOLD, 25));
		poenitext.setFill(Color.PALEGREEN);
		poenitext.setStyle(" -fx-stroke: black; -fx-stroke-width: 1;");
		poenitext.toFront();
		this.alt_root.getChildren().add(this.poenitext);

		this.graph = new Graph();
		this.graph.getTransforms().addAll(new Translate(0.0, 640.0), new Translate(80.0, 80.0));
		this.alt_root.getChildren().add(this.graph);

		timerLabel.setText(timeSeconds.toString());
		timerLabel.setTextFill(Color.BLACK);
		timerLabel.getTransforms().setAll(new Translate(WINDOW_WIDTH/2-10, 1));
		timerLabel.setStyle(" -fx-stroke: black; -fx-stroke-width: 1; -fx-font-weight: bold; -fx-font-size: 14; -fx-font-family: Time New Roman");
		timerLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%02d:%02d",timeSeconds.divide(100).get() / 60,timeSeconds.divide(100).get() % 60)));

		bar = new ProgressBar();
		bar.setPrefSize(220, 15);
		bar.setStyle(" -fx-accent: grey; -fx-control-inner-background: red;");
		bar.getTransforms().setAll(new Translate(510, 16),new Rotate(-180, 0, 0));
		//bar.progressProperty().bind(timeSeconds.divide(STARTTIME*100.0).subtract(1).multiply(-1)); //matematika zato sto odbrojava unazad
		bar.setProgress(0);
		task = new Timeline(new KeyFrame(Duration.seconds(STARTTIME+1), new KeyValue(timeSeconds, 0)));
		//task.setCycleCount(Timeline.INDEFINITE);
		timeSeconds.addListener((ov, statusOld, statusNewNumber) -> {
			int statusNew = statusNewNumber.intValue();
			bar.progressProperty().bind(timeSeconds.divide(STARTTIME*100.0).subtract(1).multiply(-1)); //matematika zato sto odbrojava unazad
			timerLabel.textProperty().bind(Bindings.createStringBinding(() -> String.format("%02d:%02d",timeSeconds.divide(100).get() / 60,timeSeconds.divide(100).get() % 60)));

		});
		this.alt_root.getChildren().addAll(bar,timerLabel);

	}

	private boolean reset() {
		this.alt_root.getChildren().remove(ballsLeftList.get(ballsLeft-1));
		ballsLeftList.remove(ballsLeft-1);
		ballsLeft--;
		if (ballsLeft > 0 && timeSeconds.get()>0) {
			this.ballPosition.setX(-900.0);this.ballPosition.setY(-55.0);ballPosition.setZ(900);
			if(podium_type==1){
				this.ballPosition.setX(-900.0);this.ballPosition.setY(-55.0);ballPosition.setZ(-150);
			}
			if(podium_type==2){
				this.ballPosition.setX(0);this.ballPosition.setY(-55.0);ballPosition.setZ(0);
			}
			this.ball.resetSpeed();
			this.arena.reset();
		}
		else if(ballsLeft==0 || timeSeconds.get()<=0){

			Label lbl= new Label("GAME OVER");
			lbl.setFont(new Font("Arial Black", 50));
			lbl.setTranslateX(WINDOW_WIDTH/2-120);
			lbl.setTranslateY(WINDOW_WIDTH/2-40);
			this.alt_root.getChildren().add(lbl);
			task.stop();
		}
		if(ballsLeft==0 ){
			return true;
		}
		if(timeSeconds.get()==0){
			return true;
		}
		return false;
	}

	private void addtokens() {
		PhongMaterial coinMaterial = new PhongMaterial(Color.GOLD);

		Cylinder b1=null,b2=null,b3=null,b4=null;
		b1=new Cylinder(50, 5);//lopta radius,5
		b2=new Cylinder(50, 5);
		b3=new Cylinder(50, 5);
		b4=new Cylinder(50, 5);
		b1.setMaterial(coinMaterial);b2.setMaterial(coinMaterial);b3.setMaterial(coinMaterial);b4.setMaterial(coinMaterial);
		Translate lower_hover = new Translate(0, -5.0, 0.0);//dokle se spusti novcic -5
		Rotate rotateY = new Rotate(0.0, Rotate.Y_AXIS);

		if(podium_type==0){
			b1.getTransforms().addAll(lower_hover,new Rotate(0, Rotate.Y_AXIS), new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//Rotate.Z_AXIS- da stoji uspravno ,rotate sa stepenima je da bi se novcici postavili na one pozicije a ne svi na istu
			b2.getTransforms().addAll(lower_hover,new Rotate(90.0, Rotate.Y_AXIS), new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//
			b3.getTransforms().addAll(lower_hover, new Rotate(180, Rotate.Y_AXIS),new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));
			b4.getTransforms().addAll(lower_hover,new Rotate(270, Rotate.Y_AXIS), new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));
		}
		if(podium_type==1){
			b1.getTransforms().addAll(lower_hover,new Rotate(0, Rotate.Y_AXIS), new Translate(450, -50, 450),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//Rotate.Z_AXIS- da stoji uspravno ,rotate sa stepenima je da bi se novcici postavili na one pozicije a ne svi na istu
			b2.getTransforms().addAll(lower_hover,new Rotate(90.0, Rotate.Y_AXIS), new Translate(450, -50, 450),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//
			b3.getTransforms().addAll(lower_hover, new Rotate(180, Rotate.Y_AXIS),new Translate(450, -50, 450),rotateY, new Rotate(90.0, Rotate.Z_AXIS));
			b4.getTransforms().addAll(lower_hover,new Rotate(270, Rotate.Y_AXIS), new Translate(450, -50, 450),rotateY, new Rotate(90.0, Rotate.Z_AXIS));

		}
		if(podium_type==2){
			b1.getTransforms().addAll(lower_hover,new Rotate(0, Rotate.Y_AXIS), new Translate(800, -50, 800),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//Rotate.Z_AXIS- da stoji uspravno ,rotate sa stepenima je da bi se novcici postavili na one pozicije a ne svi na istu
			b2.getTransforms().addAll(lower_hover,new Rotate(90.0, Rotate.Y_AXIS), new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));//
			b3.getTransforms().addAll(lower_hover, new Rotate(180, Rotate.Y_AXIS),new Translate(450, -50, 0),rotateY, new Rotate(90.0, Rotate.Z_AXIS));
			b4.getTransforms().addAll(lower_hover,new Rotate(270, Rotate.Y_AXIS), new Translate(800, -50, 800),rotateY, new Rotate(90.0, Rotate.Z_AXIS));

		}
		Timeline timeline = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(rotateY.angleProperty(),0,Interpolator.LINEAR),new KeyValue(lower_hover.yProperty(),-2)),new KeyFrame(Duration.seconds(2), new KeyValue(rotateY.angleProperty(),180,Interpolator.LINEAR),new KeyValue(lower_hover.yProperty(),-60)),new KeyFrame(Duration.seconds(4), new KeyValue(rotateY.angleProperty(),360,Interpolator.LINEAR),new KeyValue(lower_hover.yProperty(),-2)));
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.play();
		tokens.add(b1);tokens.add(b2);tokens.add(b3);tokens.add(b4);
		this.arena.getChildren().addAll(this.tokens);


	}

	private void addLight() {
		this.lampBox = new Group();
		lampColor = new PhongMaterial(Color.GRAY);
		Image selfIllumination = new Image(("selfIllumination.png"));
		lampColor.setSelfIlluminationMap(selfIllumination);
		Box box = new Box(120, 120, 120);
		box.setMaterial(this.lampColor);
		this.lampBox.getChildren().add(box);
		this.light = new PointLight(Color.WHITE);
		this.lampBox.getChildren().add(this.light);
		this.lampBox.getTransforms().add(new Translate(0.0, -1000, 0.0));
		this.root.getChildren().add(this.lampBox);
		this.isLightOn = true;
	}


	@Override
	public void start(final Stage stage) throws IOException {
		this.stage=stage;
		this.ballsLeft=5;
		final boolean[] outOfArena = new boolean[1];
		final boolean[] isInHole = new boolean[1];

		GridPane gridL = new GridPane();

		gridL.setAlignment(Pos.CENTER);
		//gridL.setPrefHeight(20);
		gridL.setHgap(20);
		gridL.setVgap(50);
		border.setCenter(gridL);

		HBox hboxlevel = new HBox();
		Label lbLvl= new Label("Choose a surface! :)");
		lbLvl.setFont(new Font("Times New Roman", 40));
		lbLvl.setAlignment(Pos.CENTER);
		hboxlevel.setPrefHeight(40);
		hboxlevel.setPadding(new Insets(20, 20, 20, 130));
		hboxlevel.setStyle("-fx-background-color: #33AFFF;");
		border.setTop(hboxlevel);
		hboxlevel.getChildren().addAll( lbLvl);

		Button buttonCurrent = new Button("Purple podium");
		buttonCurrent.setPrefSize(300, 90);
		buttonCurrent.setFont(new Font("Arial Black",25));
		buttonCurrent.setStyle(" -fx-text-fill: #FFFFFF; -fx-background-color: purple;");
		//akcija dugmeta
		buttonCurrent.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				chooseShooterSet(0);
			}
		});

		Button buttonProjected = new Button("Blue podium");
		buttonProjected.setPrefSize(300, 90);
		buttonProjected.setFont(new Font("Arial Black",25));
		buttonProjected.setStyle(" -fx-text-fill: #FFFFFF; -fx-background-color: blue;"); //-fx-background-color: #ff0000;
		//akcija dugmeta
		buttonProjected.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				chooseShooterSet(1);
			}
		});

		Button buttonHouse = new Button("Pink podium");
		buttonHouse.setPrefSize(300, 90);
		buttonHouse.setFont(new Font("Arial Black",25));
		buttonHouse.setStyle(" -fx-text-fill: #FFFFFF;-fx-background-color: pink; "); //-fx-background-color: #ff0000;
		//akcija dugmeta
		buttonHouse.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				chooseShooterSet(2);
			}
		});

		gridL.add(buttonCurrent,1,0);
		gridL.add(buttonProjected,1,1);
		gridL.add(buttonHouse,1,2);

		border.setCenter(gridL);



		Timer timer = new Timer(  deltaSeconds -> {
			if(this.arena!=null){
				this.arena.update(0.992);
				this.graph.update(this.arena.getXAngle(), this.arena.getZAngle(), 30.0);
			}
			//sudar sa zetonima
			for(Cylinder t: tokens){
				if(this.ball!=null && this.ball.getBoundsInParent().intersects(t.getBoundsInParent())){
					this.score += 5;
					poenitext.setText("Broj poena: "+score);
					this.arena.getChildren().remove(t);
					this.tokens.remove(t);
				}
			}
			if (this.ball != null) {
				//sudar za stubovima
				pillars.stream().forEach(obstacle -> this.ball.handleObstacleCollision(obstacle));
				//sudar sa zidovima
				walls.stream().forEach(wall -> this.ball.handleWallCollision(wall));
				//provera dal je ispala loptica
				outOfArena[0] = this.ball.update(deltaSeconds, 1000.0, -1000.0, -1000.0, 1000.0, this.arena.getXAngle(), this.arena.getZAngle(), MAX_ANGLE_OFFSET, 0, DAMP);
				//provera dal je u rupi loptica
				isInHole[0] =holes.stream().anyMatch ( hole ->
				{
					boolean goal=hole.handleCollision ( this.ball );
					if(goal){
						score+= hole.getValue();
						poenitext.setText("Broj poena: "+score);
						ball.resetSpeed();
					}
					return goal;
				});
				if ((outOfArena[0] || isInHole[0] || timeSeconds.get()==0) && reset()) {

					this.arena.getChildren().remove(this.ball);
					this.ball = null;


				}
			}
		} );
		timer.start();
		Scene newScene=new Scene(border,Main.WINDOW_WIDTH,Main.WINDOW_HEIGHT);
		//newScene.setCursor ( Cursor.NONE );

		stage.setTitle("Rolling Ball");
		stage.setResizable ( false );
		stage.setScene ( newScene );
		stage.show ( );

	}

	public void addHoles(){
		this.hole = new Hole(100.0, 10.0,new PhongMaterial(Color.YELLOW) ,new Translate(790, -30.0, -790) ,5);
		this.holes.add(hole);

		Hole h1=null,h2=null;
		if(podium_type==0){
			h1 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(0, -30.0, 0),-5 );
			h2 = new Hole(100, 10,new PhongMaterial(Color.YELLOW) ,new Translate(-790, -30.0, -790) ,5);
		}
		if(podium_type==1){
			h1 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(-790, -30.0, -790) ,-5);
			h2 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(350, -30.0, -790),-5 );
			Hole h3=new Hole(100, 10,new PhongMaterial(Color.YELLOW) ,new Translate(200, -30.0, 880) ,5);
			this.holes.add(h3);
		}
		if(podium_type==2){
			h1 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(0, -30.0, 850),-5 );
			h2 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(850, -30.0, 0) ,-5);
			Hole h3=new Hole(100, 10,new PhongMaterial(Color.YELLOW) ,new Translate(-850, -30.0, -850) ,5);
			Hole h4 = new Hole(100, 10,new PhongMaterial(Color.BLACK) ,new Translate(-870, -30.0, 0) ,-5);
			this.holes.add(h3);this.holes.add(h4);
		}

		this.holes.add(h1);this.holes.add(h2);
		arena.getChildren().addAll(this.holes);

	}
	private void handleKeyBoard(KeyEvent event) {

		if (event.getCode()==KeyCode.DIGIT2) {
			this.scene.setCamera(this.above_camera);
		} else if (event.getCode()==KeyCode.DIGIT1) {
			this.scene.setCamera(this.main_camera);
		} else if (event.getCode()==KeyCode.DIGIT0) {
			if (this.isLightOn) {
				this.lampColor.setSelfIlluminationMap(null);
				this.lampBox.getChildren().remove(light);
			}
			else {
				Image selfIllumination = new Image("selfIllumination.png");
				this.lampColor.setSelfIlluminationMap(selfIllumination);
				this.lampBox.getChildren().add(light);
			}
			this.isLightOn = !this.isLightOn;
		}

	}

	public static void main(final String[] args) {
		Application.launch(new String[0]);
	}


	public void init(int a){
		podium_type=a;
		this.root = new Group();
		this.scene = new SubScene(this.root, 800.0, 800.0, true, SceneAntialiasing.BALANCED);
		Box podium = new Box (
				Main.PODIUM_WIDTH,
				Main.PODIUM_HEIGHT,
				Main.PODIUM_DEPTH
		);
		initArenaByLevel(podium,a);
		this.main_camera = new MouseControlCamera(new Translate(0, 0, CAMERA_Z));
		main_camera.setFarClip(CAMERA_FAR_CLIP);
		this.root.getChildren().add(this.main_camera);
		this.scene.setCamera(this.main_camera);
		//ZA LOPTU
		above_camera= new PerspectiveCamera(true); above_camera.setFarClip(CAMERA_FAR_CLIP);
		Translate above_camera_Position = new Translate(0, -2500,0); //y je da se podigne od arene na tu visinu ostalo je da prati lopticu
		this.above_camera.getTransforms().addAll(above_camera_Position,this.ballPosition, new Rotate(CAMERA_X_ANGLE*2, Rotate.X_AXIS));//

		//zidovi
		this.arena = new Arena(new Node[0]);
		this.arena.getChildren().add(podium);
		this.arena.getChildren().add(this.ball);
		this.arena.getChildren().add(this.above_camera);
		for(int i=0;i<4;i++)this.arena.getChildren().add(this.walls.get(i));
		this.addtokens();

		this.addLight();
		this.addPillars();
		addHoles();
		this.root.getChildren().add(this.arena);
		this.alt_root = new Group();
		SubScene alt_scene = new SubScene(this.alt_root, 800.0, 800.0);
		this.initStatsScene();
		whole_scene = new Scene(new Group(new Node[] { this.scene, alt_scene}), WINDOW_WIDTH, WINDOW_HEIGHT, true, SceneAntialiasing.BALANCED);
		whole_scene.addEventHandler(MouseEvent.ANY, event -> this.main_camera.handleMouseEvent(event));
		whole_scene.addEventHandler(ScrollEvent.ANY, event -> this.main_camera.handleScrollEvent(event));
		whole_scene.addEventHandler(KeyEvent.ANY, event -> this.arena.handleKeyEvent(event, 30.0));
		whole_scene.addEventHandler(KeyEvent.KEY_PRESSED, event ->{
			if(!started)task.playFromStart();
			started=true;

			handleKeyBoard(event);
		});

		Image background = new Image("background.jpg");
		whole_scene.setFill(new ImagePattern(background));
		this.stage.setTitle("Rolling Ball");
		this.stage.setScene(whole_scene);
		this.stage.setResizable(false);
		this.stage.show();

	}

	public void chooseShooterSet(int pod){
		//bg=pod; //U PLAYGROUND PRAVLJENJE PODIJUMA
		border.setTop(null);
		border.setCenter(null);
		GridPane gridS = new GridPane();
		gridS.setAlignment(Pos.CENTER);
		//gridS.setPrefHeight(20);
		gridS.setHgap(20);
		gridS.setVgap(50);
		border.setCenter(gridS);

		HBox hboxshooter = new HBox();
		Label lblSh= new Label("Choose a ball! :)");
		lblSh.setFont(new Font("Times New Roman", 40));
		lblSh.setAlignment(Pos.CENTER);
		hboxshooter.setPrefHeight(40);
		hboxshooter.setPadding(new Insets(20, 20, 20, 130));
		hboxshooter.setStyle("-fx-background-color: #33AFFF;");
		border.setTop(hboxshooter);
		hboxshooter.getChildren().addAll( lblSh);
		//DUGMAD
		Button btnSh1 = new Button("MIN SPEED");
		btnSh1.setPrefSize(300, 90);
		btnSh1.setFont(new Font("Arial Black",25));
		btnSh1.setStyle(" -fx-text-fill: #FFFFFF; -fx-background-color: #FF9882;");
		//akcija dugmeta
		btnSh1.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				//max_shoot_speed= 300;
				init(pod);
			}
		});

		Button btnSh2 = new Button("MEDIUM SPEED");
		btnSh2.setPrefSize(300, 90);
		btnSh2.setFont(new Font("Arial Black",25));
		btnSh2.setStyle(" -fx-text-fill: #FFFFFF; -fx-background-color: #FF5733;"); //-fx-background-color: #ff0000;
		//akcija dugmeta
		btnSh2.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				max_shoot_speed= 300;
				init(pod);

			}
		});

		Button btnSh3 = new Button("MAX SPEED");
		btnSh3.setPrefSize(300, 90);
		btnSh3.setFont(new Font("Arial Black",25));
		btnSh3.setStyle(" -fx-text-fill: #FFFFFF; -fx-background-color: #E32800;"); //-fx-background-color: #ff0000;
		//akcija dugmeta
		btnSh3.setOnAction(new EventHandler<ActionEvent>(){
			public void handle(ActionEvent t){
				max_shoot_speed= 450;
				init(pod);
			}
		});

		gridS.add(btnSh1,1,0);
		gridS.add(btnSh2,1,1);
		gridS.add(btnSh3,1,2);
		border.setCenter(gridS);

	}

	public void initArenaByLevel(Box p,int bg){
		Color c=Color.PURPLE;
		Color b=Color.BLUE;
		this.ballPosition = new Translate(-900.0, -55.0, 900.0);
		if(bg==1){
			c=Color.BLUE;
			b=Color.DARKSEAGREEN;
			this.ballPosition = new Translate(-900.0, -55.0, -150);
		}
		if(bg==2){
			c=Color.DEEPPINK;
			b=Color.PURPLE;
			this.ballPosition = new Translate(0, -55.0, 0);
		}
		p.setMaterial(new PhongMaterial(c));//podium
		ballMaterial = new PhongMaterial(b);

		this.ball = new Ball(BALL_RADIUS, ballMaterial, ballPosition);
		//zidovi
		final PhongMaterial wallMaterial = new PhongMaterial(Color.BROWN);
		Box b1=null,b2=null,b3=null,b4=null;

		if(bg==0){
			b1=new Box(WALL_WIDTH, WALL_HEIGHT, PODIUM_WIDTH/2);//10,100,1000
			b2=new Box(WALL_WIDTH, WALL_HEIGHT, PODIUM_WIDTH/2);
			b3=new Box(WALL_WIDTH, WALL_HEIGHT, PODIUM_WIDTH/2);
			b4=new Box(WALL_WIDTH, 100.0, PODIUM_WIDTH/2);;
		}

		if(bg==1){
			b1= new Box(WALL_WIDTH,WALL_HEIGHT,PODIUM_WIDTH/3);
			b3=new Box(WALL_WIDTH,WALL_HEIGHT,PODIUM_WIDTH/3);
			b2= new Box(WALL_WIDTH,WALL_HEIGHT,PODIUM_WIDTH/2);
			b4= new Box(WALL_WIDTH,WALL_HEIGHT,PODIUM_WIDTH/2);
		}
		if(bg==2){
			b1= new Box(10,100,PODIUM_WIDTH/3);
			b2= new Box(10,100,PODIUM_WIDTH/3);
			b3= new Box(10,100,PODIUM_WIDTH/4);
			b4= new Box(10,100,PODIUM_WIDTH/4);
		}
		b1.setMaterial(wallMaterial);b2.setMaterial(wallMaterial);b3.setMaterial(wallMaterial);b4.setMaterial(wallMaterial);
		if(bg==0){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
		}
		if(bg==1){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(990.0, -55.0, -470));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(990.0, -55.0, 470));
		}
		if(bg==2){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(990, -55.0, 650.0));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(990, -55.0, 650.0));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(990.0, -55.0, 0.0));
		}
		this.walls.add(b1);this.walls.add(b2);this.walls.add(b3);this.walls.add(b4);

	}

	private void addPillars() {
		Image img = new Image("obstacle.jpg");
		PhongMaterial pillarMat = new PhongMaterial();
		pillarMat.setDiffuseMap(img);
		Image img2=new Image (("special.jpg"));
		PhongMaterial specMat= new PhongMaterial();
		specMat.setDiffuseMap(img2);
		Cylinder b1=null,b2=null,b3=null,b4=null;
		b1=new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT);//50,200
		b2=new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT);
		b3=new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT);
		b4=new Cylinder(OBSTACLE_RADIUS, OBSTACLE_HEIGHT);
		b1.setMaterial(pillarMat);b2.setMaterial(pillarMat);b3.setMaterial(specMat);b4.setMaterial(pillarMat);
		special_pillar=b3;

		if(podium_type==0){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(450, -110.0, 450));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(450, -110.0, 450));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(450, -110, 450));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(450, -110, 450));
		}
		if(podium_type==1){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(450, -100, 0));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(450, -100, 0.0));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(450, -100, 0));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(450, -100, 0));
		}
		if(podium_type==2){
			b1.getTransforms().addAll(new Rotate(0, Rotate.Y_AXIS), new Translate(450, -100, 0));
			b2.getTransforms().addAll(new Rotate(90, Rotate.Y_AXIS), new Translate(450, -100, 450));
			b3.getTransforms().addAll(new Rotate(180, Rotate.Y_AXIS), new Translate(450, -100, 450));
			b4.getTransforms().addAll(new Rotate(270, Rotate.Y_AXIS), new Translate(450, -100, 0));
		}
		pillars.add(b1);pillars.add(b2);pillars.add(b3);pillars.add(b4);
		this.arena.getChildren().addAll(this.pillars);
	}
	public static double getSpeed(){
		return max_shoot_speed;
	}
}
