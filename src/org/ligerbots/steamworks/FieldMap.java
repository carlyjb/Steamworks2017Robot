package org.ligerbots.steamworks;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import java.util.LinkedList;
import java.util.List;
import org.ligerbots.steamworks.commands.DrivePathCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A map of the field, complete with useful locations and obstacles.
 */
public class FieldMap {
  private static final Logger logger = LoggerFactory.getLogger(FieldMap.class);

  public static final int FIELD_SIDE_BOILER = 0;
  public static final int FIELD_SIDE_CENTER = 1;
  public static final int FIELD_SIDE_FEEDER = 2;

  private static FieldMap red;
  private static FieldMap blue;

  static {
    // +x: blue
    // -x: red
    // +y: feeder side
    // -y: boiler side
    // 0: boiler side
    // 1: middle
    // 2: feeder side
    // robot starting positions are in the middle of the alliance station
    red = new FieldMap();
    red.startingPositions[0] = new FieldPosition(-325.688, -89.060);
    red.startingPositions[1] = new FieldPosition(-325.688, -16.475);
    red.startingPositions[2] = new FieldPosition(-325.688, 87.003);
    red.boiler = new FieldPosition(-320.133, -155.743);
    red.loadingStationInner = new FieldPosition(311.673, 130.640);
    red.loadingStationOuter = new FieldPosition(268.352, 152.109);
    red.loadingStationOverflow = new FieldPosition(-325.778, 34.191);
    red.hopperBoilerRed = new FieldPosition(-205.203, -157.660);
    red.hopperBoilerCenter = new FieldPosition(0.000, -157.660);
    red.hopperBoilerBlue = new FieldPosition(205.203, -157.600);
    red.hopperLoadingRed = new FieldPosition(-119.243, 157.660);
    red.hopperLoadingBlue = new FieldPosition(119.243, 157.660);
    red.gearLiftPositions[0] = new FieldPosition(-196.685, -30.000);
    red.gearLiftPositions[1] = new FieldPosition(-213.171, 0.000);
    red.gearLiftPositions[2] = new FieldPosition(-196.685, 30.000);
    red.dividerLift12 =
        new FieldLine(new FieldPosition(-212.015, -20.216), new FieldPosition(-232.883, -32.071));
    red.dividerLift23 =
        new FieldLine(new FieldPosition(-212.015, 20.216), new FieldPosition(-232.883, 32.071));
    red.ropeStations[0] = new FieldPosition(-146.602, -52.411);
    red.ropeStations[1] = new FieldPosition(-237.683, 0);
    red.ropeStations[2] = new FieldPosition(-146.602, 52.411);

    blue = new FieldMap();
    blue.startingPositions[0] = red.startingPositions[0].multiply(-1, 1);
    blue.startingPositions[1] = red.startingPositions[1].multiply(-1, 1);
    blue.startingPositions[2] = red.startingPositions[2].multiply(-1, 1);
    blue.boiler = red.boiler.multiply(-1, 1);
    blue.loadingStationInner = red.loadingStationInner.multiply(-1, 1);
    blue.loadingStationOuter = red.loadingStationOuter.multiply(-1, 1);
    blue.loadingStationOverflow = red.loadingStationOverflow.multiply(-1, 1);
    blue.hopperBoilerRed = red.hopperBoilerRed;
    blue.hopperBoilerCenter = red.hopperBoilerCenter;
    blue.hopperBoilerBlue = red.hopperBoilerBlue;
    blue.hopperLoadingRed = red.hopperLoadingRed;
    blue.hopperLoadingBlue = red.hopperLoadingBlue;
    blue.gearLiftPositions[0] = red.gearLiftPositions[0].multiply(-1, 1);
    blue.gearLiftPositions[1] = red.gearLiftPositions[1].multiply(-1, 1);
    blue.gearLiftPositions[2] = red.gearLiftPositions[2].multiply(-1, 1);
    blue.dividerLift12 = red.dividerLift12.multiply(-1, 1);
    blue.dividerLift23 = red.dividerLift23.multiply(-1, 1);
    blue.ropeStations[0] = red.ropeStations[0].multiply(-1, 1);
    blue.ropeStations[1] = red.ropeStations[1].multiply(-1, 1);
    blue.ropeStations[2] = red.ropeStations[2].multiply(-1, 1);
  }

  public static FieldMap getRed() {
    return red;
  }

  public static FieldMap getBlue() {
    return blue;
  }

  /**
   * Gets the current alliance FieldMap.
   * 
   * @return Either red map or blue map
   */
  public static FieldMap getAllianceMap() {
    Alliance alliance = DriverStation.getInstance().getAlliance();
    if (alliance == Alliance.Blue) {
      return getBlue();
    } else if (alliance == Alliance.Red) {
      return getRed();
    } else {
      logger.error("Invalid alliance reported by DS!");
      return getBlue();
    }
  }

  private FieldMap() {}

  public FieldPosition[] startingPositions = new FieldPosition[3];
  public FieldPosition boiler;
  public FieldPosition loadingStationInner;
  public FieldPosition loadingStationOuter;
  public FieldPosition loadingStationOverflow;
  public FieldPosition hopperBoilerRed;
  public FieldPosition hopperBoilerCenter;
  public FieldPosition hopperBoilerBlue;
  public FieldPosition hopperLoadingRed;
  public FieldPosition hopperLoadingBlue;
  public FieldPosition[] gearLiftPositions = new FieldPosition[3];
  public FieldLine dividerLift12;
  public FieldLine dividerLift23;
  public FieldPosition[] ropeStations = new FieldPosition[3];

  /**
   * Generates a Catmull-Rom spline for smooth navigation across a set of control points.
   * 
   * @param controlPoints The control points
   * @return The points on the spline
   */
  public static List<FieldPosition> generateCatmullRomSpline(List<FieldPosition> controlPoints) {
    LinkedList<FieldPosition> output = new LinkedList<>();

    for (int i = 1; i < controlPoints.size() - 2; i++) {
      FieldPosition p0 = controlPoints.get(i - 1);
      FieldPosition p1 = controlPoints.get(i);
      FieldPosition p2 = controlPoints.get(i + 1);
      FieldPosition p3 = controlPoints.get(i + 2);

      generateSegment(p0, p1, p2, p3, output, i == 1);
    }

    return output;
  }

  /**
   * Generates a Catmull-Rom spline segment.
   * 
   * @param p0 Control point
   * @param p1 Control point
   * @param p2 Control point
   * @param p3 Control point
   * @param output The list to add points to
   */
  private static void generateSegment(FieldPosition p0, FieldPosition p1, FieldPosition p2,
      FieldPosition p3, List<FieldPosition> output, boolean isFirst) {
    int numPoints = (int) Math.ceil(p1.distanceTo(p2) / 4.0);
    if (numPoints < 5) {
      numPoints = 5;
    }

    double t0 = 0;
    double t1 = calculateT(t0, p0, p1);
    double t2 = calculateT(t1, p1, p2);
    double t3 = calculateT(t2, p2, p3);

    double deltaT = Math.abs(t2 - t1) / (numPoints - 1);

    for (int i = isFirst ? 0 : 1; i < numPoints; i++) {
      double ti = i * deltaT + t1;
      FieldPosition a1 = p0.multiply((t1 - ti) / (t1 - t0)).add(p1.multiply((ti - t0) / (t1 - t0)));
      FieldPosition a2 = p1.multiply((t2 - ti) / (t2 - t1)).add(p2.multiply((ti - t1) / (t2 - t1)));
      FieldPosition a3 = p2.multiply((t3 - ti) / (t3 - t2)).add(p3.multiply((ti - t2) / (t3 - t2)));

      FieldPosition b1 = a1.multiply((t2 - ti) / (t2 - t0)).add(a2.multiply((ti - t0) / (t2 - t0)));
      FieldPosition b2 = a2.multiply((t3 - ti) / (t3 - t1)).add(a3.multiply((ti - t1) / (t3 - t1)));

      output.add(b1.multiply((t2 - ti) / (t2 - t1)).add(b2.multiply((ti - t1) / (t2 - t1))));
    }
  }

  private static final double alpha = 0.5;

  private static double calculateT(double ti, FieldPosition p0, FieldPosition p1) {
    double x0 = p0.x;
    double y0 = p0.y;
    double x1 = p1.x;
    double y1 = p1.y;

    double dx = x1 - x0;
    double dy = y1 - y0;

    return Math.pow(Math.sqrt(dx * dx + dy * dy), alpha) + ti;
  }
  
  /**
   * Calculates the navigation steps to go to the gear lift.
   * 
   * @param startingPositionId The starting position ID (see comment at top of file), 0-2
   * @param gearLiftPositionId The gear lift position ID, 0-2
   * @return A DrivePathCommand to do the driving
   */
  public static DrivePathCommand navigateStartToGearLift(int startingPositionId,
      int gearLiftPositionId) {
    logger.info(String.format("Calculating path, start=%d, gear=%d", startingPositionId,
        gearLiftPositionId));
    FieldMap map = getAllianceMap();

    final Alliance alliance = DriverStation.getInstance().getAlliance();

    if (startingPositionId < 0 || startingPositionId >= map.startingPositions.length) {
      startingPositionId = 0;
      logger.error("Bad starting position: " + startingPositionId);
    }

    if (gearLiftPositionId < 0 || gearLiftPositionId >= map.gearLiftPositions.length) {
      gearLiftPositionId = 0;
      logger.error("Bad gear lift position: " + gearLiftPositionId);
    }
    
    final List<FieldPosition> controlPoints = new LinkedList<FieldPosition>();

    FieldPosition startingPosition = map.startingPositions[startingPositionId];
    logger.debug(String.format("Starting position %s", startingPosition));
    FieldPosition gearLiftPosition = map.gearLiftPositions[gearLiftPositionId];
    
    // add a point behind us so the C-R spline generates correctly
    controlPoints.add(startingPosition.add(alliance == Alliance.Red ? -12 : 12, 0));

    // drive forward 2 feet
    FieldPosition initialForwardPosition =
        startingPosition.add(alliance == Alliance.Red ? 24 : -24, 0);
    logger.debug(String.format("2ft forward position %s", initialForwardPosition));
    
    controlPoints.add(initialForwardPosition);

    double initialDriveToX;
    double initialDriveToY;
    double splinePointX;
    double splinePointY;
    if (gearLiftPositionId == 1) {
      initialDriveToX =
          alliance == Alliance.Red ? gearLiftPosition.getX() - 60 : gearLiftPosition.getX() + 60;
      initialDriveToY = 0;
      splinePointX =
          alliance == Alliance.Red ? gearLiftPosition.getX() - 24 : gearLiftPosition.getX() + 24;
      splinePointY = 0;
    } else if (gearLiftPositionId == 0) {
      double angle = alliance == Alliance.Red ? 240 : 300;
      double dx = 60 * Math.cos(Math.toRadians(angle));
      double dy = 60 * Math.sin(Math.toRadians(angle));
      initialDriveToX = gearLiftPosition.getX() + dx;
      initialDriveToY = gearLiftPosition.getY() + dy;
      
      dx = 24 * Math.cos(Math.toRadians(angle));
      dy = 24 * Math.sin(Math.toRadians(angle));
      splinePointX = gearLiftPosition.getX() + dx;
      splinePointY = gearLiftPosition.getY() + dy;
    } else {
      double angle = alliance == Alliance.Red ? 120 : 60;
      double dx = 60 * Math.cos(Math.toRadians(angle));
      double dy = 60 * Math.sin(Math.toRadians(angle));
      initialDriveToX = gearLiftPosition.getX() + dx;
      initialDriveToY = gearLiftPosition.getY() + dy;
      
      dx = 24 * Math.cos(Math.toRadians(angle));
      dy = 24 * Math.sin(Math.toRadians(angle));
      splinePointX = gearLiftPosition.getX() + dx;
      splinePointY = gearLiftPosition.getY() + dy;
    }

    FieldPosition initialDriveToPosition = new FieldPosition(initialDriveToX, initialDriveToY);
    logger.debug(String.format("Drive to position %s", initialDriveToPosition));
    controlPoints.add(initialDriveToPosition);
    
    FieldPosition splinePoint = new FieldPosition(splinePointX, splinePointY);
    logger.debug(String.format("Spline point %s", splinePoint));
    controlPoints.add(splinePoint);
    
    controlPoints.add(gearLiftPosition);
    
    logger.info(controlPoints.toString());
    List<FieldPosition> splinePoints = generateCatmullRomSpline(controlPoints);
    logger.info(splinePoints.toString());
    
    DrivePathCommand drivePathCommand = new DrivePathCommand(splinePoints);
    return drivePathCommand;
  }

  /**
   * Generates navigation to the boiler from the far side gear lift.
   * @param currentPosition The current robot position
   */
  public static DrivePathCommand navigateFeederSideLiftToBoiler(RobotPosition currentPosition) {
    logger.info(String.format("Calculating path, start=%s", currentPosition));
    FieldMap map = getAllianceMap();

    FieldPosition boiler = map.boiler;
    Alliance alliance = DriverStation.getInstance().getAlliance();
    
    FieldPosition spline0 = currentPosition.add(alliance == Alliance.Red ? 1 : -1, 0);
    final double clearX = 282;
    FieldPosition clearOfDividersPosition =
        new FieldPosition(alliance == Alliance.Red ? -clearX : clearX, currentPosition.y);

    double distanceToBoiler = clearOfDividersPosition.distanceTo(boiler);

    List<FieldPosition> controlPoints = new LinkedList<FieldPosition>();
    controlPoints.add(spline0);
    controlPoints.add(currentPosition);
    controlPoints.add(clearOfDividersPosition);
    
    double ratio = ((RobotMap.MAXIMUM_SHOOTING_DISTANCE + RobotMap.MINIMUM_SHOOTING_DISTANCE) / 2)
        / distanceToBoiler;
    
    controlPoints.add(clearOfDividersPosition.multiply(1 - ratio).add(boiler.multiply(ratio)));
    
    controlPoints.add(boiler);
    List<FieldPosition> splinePoints = generateCatmullRomSpline(controlPoints);
    DrivePathCommand drivePathCommand = new DrivePathCommand(splinePoints);
    return drivePathCommand;
  }
}
