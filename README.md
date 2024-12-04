# Farm Management System

A Farm Management System (FMS) using drones integrates advanced technologies like drones, sensors, and data analytics to enhance agricultural practices. It automates and streamlines farming operations, providing real-time data, precision monitoring, and decision-making tools to improve productivity, sustainability, and efficiency.

## Overview
**Farm Management System** is a Java-based application built using Eclipse. It utilizes JavaFX for the user interface and adheres to standard project configurations. This repository contains the source code, build files, and necessary configurations to run the project.

## Features
- Build a dashboard to setup items on the farm.
- For each item users should be able to provide basic details such as location coordinates, dimensions, price and others.
- Following use cases are being included in this part of the implementation:
  1) manage farm items - [actor: farmer];
  2) visit item [actors: drone, farmer];
  3) scan farm [actors: drone, farmer]; and
  4) create farm layout [actors: farmer].
- The dashboard should implement singleton, composite design patterns and create a visualization of the items added to the farm.



## Project Structure
The project is organized as follows:
- **Root**
  - **bin/**
    - **application/**
      - `application.css`
      - `background.png`
      - `Dashboard_Background.jpg`
      - `drone.jpg`
      - `drone.png`
      - `DroneAnimation.class`
      - `FarmItem.class`
      - `Item.class`
      - `ItemContainer.class`
      - `Main.class`
      - `MainController.class`
      - `MainScene.fxml`
  - **src/**
    - **application/**
      - `application.css`
      - `background.png`
      - `Dashboard_Background.jpg`
      - `drone.jpg`
      - `drone.png`
      - `DroneAnimation.java`
      - `FarmItem.java`
      - `Item.java`
      - `ItemContainer.java`
      - `Main.java`
      - `MainController.java`
      - `MainScene.fxml`


## Requirements
- **Java Development Kit (JDK)** version 8 or later.
- **Eclipse IDE**
- **JavaFX SDK** for UI components.
  

## Link to Git repository
1. Clone the repository:
   ```bash
   git clone https://github.com/Nagama30/Farm_Management_System_With_Drone_Automation/tree/main

