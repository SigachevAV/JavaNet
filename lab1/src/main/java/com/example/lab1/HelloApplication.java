package com.example.lab1;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.TriangleMesh;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application
{
    private Group board;
    private Circle target1 = new Circle(50, Color.RED);
    private Circle target2 = new Circle(20, Color.RED);
    private Rectangle player = new Rectangle(50, 50, Color.BLACK);
    private TargetThread targetThread1 = new TargetThread();
    private TargetThread targetThread2 = new TargetThread();
    private Label scoreLabel = new Label("Score: 0");
    private Label shootsLabel = new Label("Shoots: 0");

    private int score = 0;
    private int shoots = 0;

    private void TargetInit()
    {
        targetThread1.SetTarget(target1, 500, 200, 20);
        targetThread2.SetTarget(target2, 400, 200, 10);
    }

    private void RefillTarget()
    {
        board.getChildren().addAll(target1, target2);
        target1.relocate(500, 200);
        target2.relocate(400, 200);
        targetThread1.resume();
        targetThread2.resume();
        score+=1;
    }

    private void OnHitChec()
    {
        if (player.getBoundsInParent().intersects(target1.getBoundsInParent()))
        {
            board.getChildren().remove(target1);
            targetThread1.suspend();
        }
        if (player.getBoundsInParent().intersects(target2.getBoundsInParent()))
        {
            targetThread2.suspend();
            board.getChildren().remove(target2);
        }
        if(!board.getChildren().contains(target1) && !board.getChildren().contains(target2) && player.getLayoutX() == 10)
        {
            RefillTarget();
        }
    }

    @Override
    public void start(Stage stage) throws IOException
    {
        board = new Group();
        board.getChildren().addAll(player, target1, target2, scoreLabel, shootsLabel);
        shootsLabel.relocate(10, 10);
        scoreLabel.relocate(10, 30);
        Scene scene = new Scene(board, 600, 400, Color.WHITE);
        player.relocate(10, 200);
        stage.setTitle("Lab1");
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                switch (keyEvent.getCode())
                {
                    case SPACE:
                        shoots+=1;
                        PlayerThread playerThread = new PlayerThread();
                        playerThread.SetPlayer(player);
                        if(!playerThread.isAlive()) {
                            playerThread.start();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                shootsLabel.setText("Shoots: "+shoots);
                scoreLabel.setText("Score: "+score);
                OnHitChec();
            }
        };
        timer.start();
        stage.setScene(scene);
        TargetInit();
        targetThread1.start();
        targetThread2.start();
        stage.show();
    }
    @Override
    public void stop() throws Exception
    {
        targetThread2.stop();
        targetThread1.stop();
        super.stop();
    }
    public static void main(String[] args)
    {
        launch();
    }



    private class MovTarget implements Runnable
    {
        private Shape shape;
        private double newY;
        private double newX;
        public  MovTarget(Shape _shape, double _newX, double _newY)
        {
            this.shape = _shape;
            this.newY=_newY;
            this.newX = _newX;
        }
        @Override
        public void run()
        {
            shape.relocate(this.newX, this.newY);
        }
    }

    private class PlayerThread extends Thread
    {
        private Rectangle player;
        private double playerW;
        private double playerH;
        public void SetPlayer(Rectangle _player)
        {
            this.player=_player;
            this.playerW=this.player.getLayoutX();
            this.playerH=this.player.getLayoutY();
        }

        @Override
        public void run()
        {
            double pos = this.playerW;
            while (true)
            {
                pos+=1;
                Platform.runLater(new MovTarget(this.player, pos, this.playerH));
                try
                {
                    Thread.sleep(1);
                }
                catch (Exception exception)
                {
                    return;
                }
                if(pos>600)
                {
                    player.relocate(10, 200);
                    return;
                }
            }
        }
    }

    private class TargetThread extends Thread
    {
        private Circle target;
        private int speed;
        private int startH;
        private int startW;
        public void SetTarget(Circle _target, int _w, int _h, int _speed)
        {
            this.target=_target;
            this.target.relocate(_w, _h);
            this.speed=_speed;
            this.startH=_h;
            this.startW=_w;
        }
        @Override
        public void run()
        {
            int pos = startH;
            int vec = 1;
            while (true)
            {
                try
                {
                    Thread.sleep(speed);
                    Thread.yield();
                }
                catch (Exception exception)
                {
                    return;
                }
                pos+=vec;
                Platform.runLater(new MovTarget(this.target, startW, pos));
                if(pos>400 || pos<0)
                {
                    vec=vec*(-1);
                }
            }
        }
    }
}

