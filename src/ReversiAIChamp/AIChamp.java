
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.time.*;


/**
 * Created by Jason on 11/8/17.
 */
public class AIChamp {

     int MAXDEPTH = 7;
     double MAX = -1;
    static double MIN = .1;
    static double MULT = 10;

    static int MAX_TURN_LENGTH = 5; // in seconds

    enum PlayerType {MINIMIZER, MAXIMIZER}

    public Socket s;
    public BufferedReader sin;
    public PrintWriter sout;
    Random generator = new Random();

    double t1, t2;
    int me;
    int them;
    int boardState;
    int curState[][] = new int[8][8]; // state[0][0] is the bottom left corner of the board (on the GUI)
    int turn = -1;
    int round;


    Instant current;

    //int validMoves[] = new int[64];
    //int numValidMoves;

    public AIChamp(int _me, String host) {
        me = _me;
        if (me == 1) {
            them = 2;
        }
        if (me == 2) {
            them = 1;
        }

        initClient(host);

        int myMove;

        while (true) {
            System.out.println("Read");
            readMessage();


            if (turn == me) {

                if (MAX == -1) {
                    if (me==1) {
                        MAX = t1;

                    }else{
                        MAX = t1;
                    }
                }else {

                    if (me == 1) {
                        MAXDEPTH = (int)(( t1 - MIN) / (MAX - MIN) * MULT);
                    } else {
                        MAXDEPTH = (int)(( t2 - MIN) / (MAX - MIN) * MULT);
                    }

                }



                System.out.println("Move " + "MAXDEPTH: " + MAXDEPTH);
                List<Integer> validMoves = getValidMoves(round, curState);


                current = Instant.now();
                myMove = move(curState, round, validMoves);



                String sel = myMove / 8 + "\n" + myMove % 8;

                System.out.println("Selection: " + myMove / 8 + ", " + myMove % 8);

                sout.println(sel);
            }
        }
        //while (turn == me) {
        //    System.out.println("My turn");

        //readMessage();
        //}
    }

    /**
     * This is the recursive minimax algorithm with alpha/beta pruning.
     *
     * @param state        the current state
     * @param round        current round
     * @param depth        how far we have recursed down the gametree
     * @param parentchoice what our maximizer/minimizer parent will choose if we do not supply a larger/smaller value
     * @return minimum/maximum child value if we are minimizer/maximizer
     */
    private float minimax(int[][] state, int round, int depth, float parentchoice, PlayerType type) {
        //System.out.println("ROUND:" +  round + " DEPTH: " + depth +  " PARENTCOICE: " + parentchoice + " TYPE: " + type);
        List<Integer> moves = getValidMoves(round, state);


//        if (depth > MAXDEPTH || moves.size() == 0) {
        Long duration = Duration.between(current, Instant.now()).getSeconds();
//        if (MAXDEPTH < depth || duration > MAX_TURN_LENGTH|| moves.size() == 0) {
            if (MAXDEPTH < depth || moves.size() == 0) {
      //  System.out.println("DURATION: " + duration  + " DEPTH: " + depth);
            return heuristic(state, round);
        } else {

            float choice = (type == PlayerType.MINIMIZER) ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
            PlayerType childtype = (type == PlayerType.MINIMIZER) ?
                    PlayerType.MAXIMIZER : PlayerType.MINIMIZER;

            int mi = 0;
            for (Integer m : moves) {

                int turn = (type == PlayerType.MINIMIZER) ? them : me;
                int[][] newState = getNewState(state, m, turn - 1);

                float childchoice = minimax(newState,
                        round + 1, depth + 1, choice, childtype);

                if ((choice > childchoice && type == PlayerType.MINIMIZER)
                        || (childchoice > choice && type == PlayerType.MAXIMIZER)) {
                    choice = childchoice;
                    mi = m;
                }

                // Alpha/beta pruning branch
                if ((choice <= parentchoice && type == PlayerType.MINIMIZER)
                        || (parentchoice >= choice && type == PlayerType.MAXIMIZER)) {
                    //  System.out.println("My move{" +( m / 8) + ","  + (m % 8) + "} choice: " + choice + " depth: " + depth + " type: " + type);
                    return choice;
                }
            }
            //System.out.println("My move{" +( mi / 8) + ","  + (mi % 8) + "}choice: " + choice + " depth: " + depth + " type: " + type);
            return choice;
        }
    }

    private int[][] getNewState(int[][] state, int move, int turn) {

        int[][] newState = new int[8][8];
        for (int i = 7; i >= 0; i--) {
            for (int j = 0; j < 8; j++) {
                newState[i][j] = state[i][j];
            }
        }


        int row = move / 8;
        int col = move % 8;
        newState = changeColors(newState, row, col, turn);
//        System.out.println("CHILD Turn: " + turn);
//        for (int i = 7; i >= 0; i--) {
//            for (int j = 0; j < 8; j++) {
//                System.out.print(" " + state[i][j] + "->" + newState[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.println();

        return newState;
    }

    static int[][] pointMatrix =  {
            {10, 0, 3, 5, 5, 3, 0, 10},
            { 0, 0, 1, 2, 2, 1, 0, 0 },
            { 3, 1, 2, 2, 2, 2, 1, 3 },
            { 5, 2, 2, 2, 2, 2, 2, 5 },
            { 5, 2, 2, 2, 2, 2, 2, 5 },
            { 3, 1, 2, 2, 2, 2, 1, 3 },
            { 0, 0, 1, 2, 2, 1, 0, 0 },
            {10, 0, 3, 5, 5, 3, 0, 10},
    };

    /**
     * The evaluation of a state based on a heuristic
     *
     * @param state the current state
     * @param round the round of the game
     * @return
     */
    private float heuristic(int[][] state, int round) {
        int[] tileStateCount = new int[3];
        int value = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (state[i][j] == me) {
                    value += pointMatrix[i][j];
                }
            }
        }
        return value;
    }

    // You should modify this function
    // validMoves is a list of valid locations that you could place your "stone" on this turn
    // Note that "state" is a global variable 2D list that shows the state of the game

    /**
     * Finds the best move
     *
     * @param state      current state of the game
     * @param validMoves integers that correspond with indices on the game board
     * @return Game board index that corresponds with the best move
     */
    private int move(int[][] state, int round, List<Integer> validMoves) {
        // just move randomly for now
        int move = validMoves.get(generator.nextInt(validMoves.size()));
        float maxchoice = Float.NEGATIVE_INFINITY;

        for (Integer m : validMoves) {
            int[][] childState = getNewState(state, m, me - 1);
            float childchoice = minimax(childState, round + 1, 0, maxchoice, PlayerType.MAXIMIZER);
            //  System.out.println("CHILD CHOICE: " + childchoice);

            if (childchoice > maxchoice) {
                maxchoice = childchoice;
                move = m;
                //  System.out.println("MY CHOICE: " + maxchoice + " MOVE{" +( move / 8) + ","  + (move % 8) + "}");

            }

        }

        return move;
    }

    // generates the set of valid moves for the player; returns a list of valid moves (validMoves)
    List<Integer> getValidMoves(int round, int state[][]) {
        List<Integer> validMoves = new ArrayList<>(64);
        int i, j;

        if (round < 4) {
            if (state[3][3] == 0) {
                validMoves.add(3 * 8 + 3);
            }
            if (state[3][4] == 0) {
                validMoves.add(3 * 8 + 4);
            }
            if (state[4][3] == 0) {
                validMoves.add(4 * 8 + 3);
            }
            if (state[4][4] == 0) {
                validMoves.add(4 * 8 + 4);
            }
            // System.out.println("Valid Moves:");
            //  for (i = 0; i < validMoves.size(); i++) {
            //   System.out.println(validMoves.get(i / 8) + ", " + validMoves.get(i % 8));
            // }
        } else {
            //  System.out.println("Valid Moves:");
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    if (state[i][j] == 0) {
                        if (couldBe(state, i, j)) {
                            validMoves.add(i * 8 + j);
                            // System.out.println(i + ", " + j + "= " + ((i * 8) + j));
                        }
                    }
                }
            }
        }


        //if (round > 3) {
        //    System.out.println("checking out");
        //    System.exit(1);
        //}
        return validMoves;

    }

    /**
     * The original checkDirection from the client
     *
     * @param state
     * @param row
     * @param col
     * @param incx
     * @param incy
     * @return
     */
    private boolean checkDirection(int state[][], int row, int col, int incx, int incy) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;

        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row + incy * i;
            c = col + incx * i;

            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;

            sequence[seqLen] = state[r][c];
            seqLen++;
        }

        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (me == 1) {
                if (sequence[i] == 2)
                    count++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        return true;
                    break;
                }
            } else {
                if (sequence[i] == 1)
                    count++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        return true;
                    break;
                }
            }
        }

        return false;
    }

    public static int[][] changeColors(int[][] state, int row, int col, int turn) {
        int incx, incy;


        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    state[row][col] = turn + 1;
                //continue;

                state = checkDirection(state, row, col, incx, incy, turn);
            }
        }
        return state;
    }

    /**
     * The checkDirection from the server. It will MODIFY the state.
     *
     * @param state
     * @param row
     * @param col
     * @param incx
     * @param incy
     * @return
     */
    public static int[][] checkDirection(int[][] state, int row, int col, int incx, int incy, int turn) {
        int sequence[] = new int[7];
        int seqLen;
        int i, r, c;

        seqLen = 0;
        for (i = 1; i < 8; i++) {
            r = row + incy * i;
            c = col + incx * i;

            if ((r < 0) || (r > 7) || (c < 0) || (c > 7))
                break;

            sequence[seqLen] = state[r][c];
            seqLen++;
        }

        int count = 0;
        for (i = 0; i < seqLen; i++) {
            if (turn == 0) {
                if (sequence[i] == 2)
                    count++;
                else {
                    if ((sequence[i] == 1) && (count > 0))
                        count = 20;
                    break;
                }
            } else {
                if (sequence[i] == 1)
                    count++;
                else {
                    if ((sequence[i] == 2) && (count > 0))
                        count = 20;
                    break;
                }
            }
        }

        if (count > 10) {
            if (turn == 0) {
                i = 1;
                r = row + incy * i;
                c = col + incx * i;
                while (state[r][c] == 2) {
                    state[r][c] = 1;
                    i++;
                    r = row + incy * i;
                    c = col + incx * i;
                }
            } else {
                i = 1;
                r = row + incy * i;
                c = col + incx * i;
                while (state[r][c] == 1) {
                    state[r][c] = 2;
                    i++;
                    r = row + incy * i;
                    c = col + incx * i;
                }
            }
        }

        return state;
    }

    private boolean couldBe(int state[][], int row, int col) {
        int incx, incy;

        for (incx = -1; incx < 2; incx++) {
            for (incy = -1; incy < 2; incy++) {
                if ((incx == 0) && (incy == 0))
                    continue;

                if (checkDirection(state, row, col, incx, incy))
                    return true;
            }
        }

        return false;
    }

    public void readMessage() {
        int i, j;
        String status;
        try {
            //System.out.println("Ready to read again");
            turn = Integer.parseInt(sin.readLine());

            if (turn == -999) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println(e);
                }

                System.exit(1);
            }

            //System.out.println("Turn: " + turn);
            round = Integer.parseInt(sin.readLine());
            t1 = Double.parseDouble(sin.readLine());
          //  System.out.println(t1);
            t2 = Double.parseDouble(sin.readLine());
            System.out.println(t2);
            for (i = 0; i < 8; i++) {
                for (j = 0; j < 8; j++) {
                    curState[i][j] = Integer.parseInt(sin.readLine());
                }
            }
            sin.readLine();
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }

      //  System.out.println("Turn: " + turn);
      //  System.out.println("Round: " + round);
//        for (i = 7; i >= 0; i--) {
//            for (j = 0; j < 8; j++) {
//                System.out.print(curState[i][j]);
//            }
//            System.out.println();
//        }
//        System.out.println();
    }

    public void initClient(String host) {
        int portNumber = 3333 + me;

        try {
            s = new Socket(host, portNumber);
            sout = new PrintWriter(s.getOutputStream(), true);
            sin = new BufferedReader(new InputStreamReader(s.getInputStream()));

            String info = sin.readLine();
            System.out.println(info);
        } catch (IOException e) {
            System.err.println("Caught IOException: " + e.getMessage());
        }
    }


    // compile on your machine: javac *.java
    // call: java RandomGuy [ipaddress] [player_number]
    //   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
    //   player_number is 1 (for the black player) and 2 (for the white player)
    public static void main(String args[]) {
        new AIChamp(Integer.parseInt(args[1]), args[0]);
    }


}
