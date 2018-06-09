## 智能应答系统设计与实现

####摘要

针对2048游戏，实现一个AI程序，当用户选择提示功能时，系统根据当前局势找出适当的决策帮助用户赢得游戏。系统界面实现由Android完成，主体AI使用了传统的博弈树模型中常用的算法，即Minimax和Alpha-beta剪枝，重点落在启发函数的设计上。项目的启发函数从四个方面评估当前的局势，结合迭代深搜，在限定搜索时间内做出决策，赢得游戏的概率高于90%。

####引言

在游戏2048中，因为数字出现的随机性，使得玩家有可能会陷入困境，最终输掉游戏。通过实现一个AI程序，系统能够在局势最坏的情况下做出最有利的决策帮助玩家摆脱困境，或者是在即将陷入困境前，通过系统AI的提示进行决策，那么玩家就可以避免陷入困境，借助系统AI提供的决策功能，玩家能够轻松赢得游戏胜利。

####问题描述

##### 1. 2048游戏简介 

《2048》是一款比较流行的数字游戏，最早于2014年3月20日发行。原版2048首先在GitHub上发布，原作者是Gabriele Cirulli，后被移植到各个平台。

游戏规则：

每次可以选择上下左右其中一个方向去滑动，每滑动一次，所有的数字方块都会往滑动的方向靠拢外，系统也会在空白的地方乱数出现一个数字方块，相同数字的方块在靠拢、相撞时会相加。不断的叠加最终拼凑出2048这个数字就算成功。

#####2. 2048智能提示实现 

系统AI提供提示功能，从当前格局出发，寻找所能达到搜索层次的最优的解

点击`Hint`按钮，显示滑动提示

<img src="screenshots\Screenshot_2018-01-13-11-40-07-821_com.admin.md20.png" style="zoom:30%" />

<img src="screenshots\Screenshot_2018-01-13-11-40-40-632_com.admin.md20.png" style="zoom:30%" />

##### 3. 基础算法

> Since the game is a discrete state space, perfect information, turn-based game like chess and checkers, I used the same methods that have been proven to work on those games, namely [minimax](http://www.flyingmachinestudios.com/programming/minimax/) [search](http://xkcd.com/832/) with [alpha-beta pruning](http://cs.ucla.edu/~rosen/161/notes/alphabeta.html). Since there is already a lot of info on that algorithm out there, I'll just talk about the two main heuristics that I use in the [static evaluation function](http://en.wikipedia.org/wiki/Evaluation_function) and which formalize many of the intuitions that other people have expressed here.

2048本质上可以抽象成信息对称双人对弈模型（玩家向四个方向中的一个移动，然后计算机在某个空格中填入2或4）。这里“信息对称”是指在任一时刻对弈双方对格局的信息完全一致，移动策略仅依赖对接下来格局的推理。作者使用的核心算法为对弈模型中常用的带Alpha-beta剪枝的Minimax。这个算法也常被用于如国际象棋等信息对称对弈AI中。

##### 4. 建模

上面说过Minimax和Alpha-beta都是针对信息对称的轮流对弈问题，这里ovolve是这样抽象游戏的：

- 我方：游戏玩家。每次可以选择上、下、左、右四个行棋策略中的一种（某些格局会少于四种，因为有些方向不可走）。行棋后方块按照既定逻辑移动及合并，格局转换完成。
- 对方：计算机。在当前任意空格子里放置一个方块，方块的数值可以是2或4。放置新方块后，格局转换完成。
- 胜利条件：出现某个方块的数值为“2048”。
- 失败条件：格子全满，且无法向四个方向中任何一个方向移动（均不能触发合并）。

如此2048游戏就被建模成一个信息完备的双人对弈问题，而传统博弈树和技巧就自然有了用武之地。

##### 5. 格局评价

作为算法的核心，如何评价当前格局的价值是重中之重。在2048中，除了终局外，中间格局并无非常明显的价值评价指标，因此需要用一些启发式的指标来评价格局。那些分数高的“好”格局是容易引向胜利的格局，而分低的“坏”格局是容易引向失败的格局。

这里采用了如下几个启发式指标。

1. 单调性

   单调性指方块从左到右、从上到下均遵从递增或递减。一般来说，越单调的格局越好。下面是一个具有良好单调格局的例子：

   ![7cc829d3gw1ef80z7ayb7j209k09jq3a](screenshots\7cc829d3gw1ef80z7ayb7j209k09jq3a.jpg)

2. 平滑性

   平滑性是指每个方块与其直接相邻方块数值的差，其中差越小越平滑。例如2旁边是4就比2旁边是128平滑。一般认为越平滑的格局越好。下面是一个具有极端平滑性的例子：

   ![7cc829d3gw1ef80z84lcmj209i09j0t5](screenshots\7cc829d3gw1ef80z84lcmj209i09j0t5.jpg)

3. 空格数

   一般来说，局面的空格总数越少对玩家越不利，所以认为空格越多的格局越好。

4. 最大数

   当前局面的最大数字, 该特征为积极因子


```java
    /**
     * 格局评估函数
     * 
     * @return 返回当前格局的评估值，用于比较判断格局的好坏
     */
    private double evaluate() {
        double smoothWeight = 0.1, //平滑性权重系数
                monoWeight = 1.3, //单调性权重系数
                emptyWeight = 2.7, //空格数权重系数
                maxWeight = 1.0; //最大数权重系数
        return grid.smoothness() * smoothWeight
                + grid.monotonicity() * monoWeight
                + Math.log(getEmptyNum(grid.getCellMatrix())) * emptyWeight
                + grid.maxValue() * maxWeight;
    }
```

采用线性函数,，并添加权重系数，前3项指标能衡量一个局面的好坏，而最大数该项，则让游戏AI多了一点积极和"冒险"。

####算法描述

##### 1. Minimax

下面先介绍不带剪枝的Minimax，首先通过一个简单的例子说明Minimax算法的思路和决策方式。

**问题**

现在考虑这样一个游戏：有三个盘子A、B和C，每个盘子分别放有三张纸币。A放的是1、20、50；B放的是5、10、100；C放的是1、5、20。单位均为“元”。有甲、乙两人，两人均对三个盘子和上面放置的纸币有可以任意查看。游戏分三步：

1. 甲从三个盘子中选取一个。
2. 乙从甲选取的盘子中拿出两张纸币交给甲。
3. 甲从乙所给的两张纸币中选取一张，拿走。

其中甲的目标是最后拿到的纸币面值尽量大，乙的目标是让甲最后拿到的纸币面值尽量小。

下面用Minimax算法解决这个问题。

**基本思路**

一般解决博弈类问题的自然想法是将格局组织成一棵树，树的每一个节点表示一种格局，而父子关系表示由父格局经过一步可以到达子格局。Minimax 也不例外，它通过对以当前格局为根的格局树搜索来确定下一步的选择。而一切格局树搜索算法的核心都是对每个格局价值的评价。Minimax算法基于以下朴素思想确定格局价值：

- Minimax是一种悲观算法，即假设对手每一步都会将我方引入从当前看理论上价值最小的格局方向，即对手具有完美决策能力。因此我方的策略应该是选择那些对方所能达到的让我方最差情况中最好的，也就是让对方在完美决策下所对我造成的损失最小。
- Minimax不找理论最优解，因为理论最优解往往依赖于对手是否足够愚蠢，Minimax中我方完全掌握主动，如果对方每一步决策都是完美的， 则我方可以达到预计的最小损失格局，如果对方没有走出完美决策，则我方可能达到比预计的最悲观情况更好的结局。总之我方就是要在最坏情况中选择最好的。

**解题**

下图是上述示例问题的格局树：

![01](screenshots\01.png)

由于示例问题格局数非常少，这里可以给出完整的格局树。这种情况下我们可以找到Minimax算法的全局最优解。而真实情况中，格局树非常庞大，即使是计算机也不可能给出完整的树，因此我们往往只搜索一定深度，这时只能找到局部最优解。

从甲的角度考虑。其中正方形节点表示轮到我方（甲），而三角形表示轮到对方（乙）。经过三轮对弈后（我方-对方-我方），将进入终局。黄色叶结点表示所有可能的结局。从甲方看，由于最终的收益可以通过纸币的面值评价，我们自然可以用结局中甲方拿到的纸币面值表示终格局的价值。

下面考虑倒数第二层节点，在这些节点上，轮到我方选择，所以我们应该引入可选择的最大价值格局，因此每个节点的价值为其子节点的最大值：

![02](screenshots\02.png)

这些轮到我方的节点称为max节点，max节点的值是其子节点最大值。

倒数第三层轮到对方选择，假设对方会尽力将局势引入让我方价值最小的格局，因此这些节点的价值取决于子节点的最小值。这些轮到对方的节点称为min节点。

最后，根节点是max节点，因此价值取决于叶子节点的最大值。最终完整赋值的格局树如下：

![03](screenshots\03.png)

**Minimax算法的步骤总结：**

1. 首先确定最大搜索深度D，D可能达到终局，也可能是一个中间格局。
2. 在最大深度为D的格局树叶子节点上，使用预定义的价值评价函数对叶子节点价值进行评价。
3. 自底向上为非叶子节点赋值。其中max节点取子节点最大值，min节点取子节点最小值。
4. 每次轮到我方时（此时必处在格局树的某个max节点），选择价值等于此max节点价值的那个子节点路径。

在上面的例子中，根节点的价值为20，表示如果对方每一步都完美决策，则我方按照上述算法可最终拿到20元，这是我方在Minimax算法下最好的决策。格局转换路径如下图红色路径所示：

![04](screenshots\04.png)

**对于真实问题中的Minimax的问题：**

- 真实问题一般无法构造出完整的格局树，所以需要确定一个最大深度D，每次最多从当前格局向下计算D层。
- 因为上述原因，Minimax一般是寻找一个局部最优解而不是全局最优解，搜索深度越大越可能找到更好的解，但计算耗时会呈指数级膨胀。
- 也是因为无法一次构造出完整的格局树，所以真实问题中Minimax一般是边对弈边计算局部格局树，而不是只计算一次，但已计算的中间结果可以缓存。

##### 2. Alpha Beta Pruning

简单的Minimax算法有一个很大的问题就是计算复杂性。由于所需搜索的节点数随最大深度呈指数膨胀，而算法的效果往往和深度相关，因此这极大限制了算法的效果。

Alpha-beta剪枝是对Minimax的补充和改进。采用Alpha-beta剪枝后，我们可不必构造和搜索最大深度D内的所有节点，在构造过程中，如果发现当前格局再往下不能找到更好的解，我们就停止在这个格局及以下的搜索，也就是剪枝。

Alpha-beta基于这样一种朴素的思想：时时刻刻记得当前已经知道的最好选择，如果从当前格局搜索下去，不可能找到比已知最优解更好的解，则停止这个格局分支的搜索（剪枝），回溯到父节点继续搜索。

**基本方法**

从根节点开始采用深度优先的方式构造格局树，在构造每个节点时，都会读取此节点的alpha和beta两个值，其中alpha表示搜索到当前节点时已知的最好选择的下界，而beta表示从这个节点往下搜索最坏结局的上界。由于我们假设对手会将局势引入最坏结局之一，因此当beta小于alpha时，表示从此处开始不论最终结局是哪一个，其上限价值也要低于已知的最优解，也就是说已经不可能此处向下找到更好的解，所以就会剪枝。

**同样以上述示例介绍Alpha-beta剪枝算法的工作原理**

1. 根节点的alpha和beta分别被初始化为$−∞$，和$+∞$。

2. 深度优先搜索第一个孩子，不是叶子节点，所以alpha和beta继承自父节点，分别为$−∞$，和$+∞$

3. 搜索第三层的第一个孩子，同上。

4. 搜索第四层，到达叶子节点，采用评价函数得到此节点的评价值为1。

   ![05](screenshots\05.png)

5. 此叶节点的父节点为max节点，因此更新其alpha值为1，表示此节点取值的下界为1。

6. 再看另外一个子节点，值为20，大于当前alpha值，因此将alpha值更新为20。

7. 此时第三层最左节点所有子树搜索完毕，作为max节点，更新其真实值为当前alpha值：20。

8. 由于其父节点（第二层最左节点）为min节点，因此更新其父节点beta值为20，表示这个节点取值最多为20。

   ![06](screenshots\06.png)

9. 搜索第二层最左节点的第二个孩子及其子树，按上述逻辑，得到值为50（注意第二层最左节点的beta值要传递给孩子）。由于50大于20，不更新min节点的beta值。

   ![07](screenshots\07.png)

10. 搜索第二层最左节点的第三个孩子。当看完第一个叶子节点后，发现第三个孩子的alpha=beta，此时表示这个节点下不会再有更好解，于是剪枝

  ![08](screenshots\08.png)

11. 继续搜索B分支，当搜索完B分支的第一个孩子后，发现此时B分支的alpha为20，beta为10。这表示B分支节点的最大取值不会超过10，而我们已经在A分支取到20，此时满足alpha大于等于beta的剪枝条件，因此将B剪枝。并将B分支的节点值设为10，注意，这个10不一定是这个节点的真实值，而只是上线，B节点的真实值可能是5，可能是1，可能是任何小于10的值。但是已经无所谓了，反正我们知道这个分支不会好过A分支，因此可以放弃了。

    ![09](screenshots\09.png)

12. 在C分支搜索时遇到了与B分支相同的情况。因此讲C分支剪枝。

    ![10](screenshots\10.png)

此时搜索全部完毕，而我们也得到了这一步的策略：应该走A分支。

可以看到相比普通Minimax要搜索18个叶子节点相比，这里只搜索了9个。采用Alpha-beta剪枝，可以在相同时间内加大Minimax的搜索深度，因此可以获得更好的效果。并且Alpha-beta的解和普通Minimax的解是一致的。

#####3. Minimax Search和Alpha Beta Pruning的实现

```java
private SearchResult search(int depth, double alpha, double beta, int positions, int cutoffs) {
    double bestScore;
    int bestMove = -1;
    SearchResult result = new SearchResult();
    int[] directions = {0, 1, 2, 3};

    if (this.grid.playerTurn) {  // Max 层
        bestScore = alpha;
        for (int direction : directions) {  // 玩家遍历四个滑动方向，找出一个最好的
            GameState newGrid = new GameState(this.grid.getCellMatrix());
            if (newGrid.move(direction)) {
                positions++;
//                if (newGrid.isWin()) {
//                    return new SearchResult(direction, 10000, positions, cutoffs);
//                }
                AI newAI = new AI(newGrid);
                newAI.grid.playerTurn = false;

                if (depth == 0) { //如果depth=0,搜索到该层后不再向下搜索
                    result.move = direction;
                    result.score = newAI.evaluate();
                } else { //如果depth>0,则继续搜索下一层，下一层为电脑做出决策的层
                    result = newAI.search(depth - 1, bestScore, beta, positions, cutoffs);
                    if (result.score > 9900) { // 如果赢得游戏
                        result.score--; // 轻微地惩罚因为更大的搜索深度
                    }
                    positions = result.positions;
                    cutoffs = result.cutoffs;
                }

                //如果当前搜索分支的格局分数要好于之前得到的分数，则更新决策，同时更新bestScore，也即alpha的值
                if (result.score > bestScore) {
                    bestScore = result.score;
                    bestMove = direction;
                }
                //如果当前bestScore也即alpha>beta时，表明这个节点下不会再有更好解，于是剪枝
                if (bestScore > beta) {
                    cutoffs++;  //剪枝
                    return new SearchResult(bestMove, beta, positions, cutoffs);
                }
            }
        }
    } else {
        // Min 层，该层为电脑层(也即我们的对手)，这里我们假设对手(电脑)足够聪明，总是能做出使格局变到最坏的决策
        bestScore = beta;

        // 尝试给每个空闲块填入2或4，然后计算格局的评估值
        List<Candidate> candidates = new ArrayList<>();
        List<int[]> cells = this.grid.getAvailableCells();
        int[] fill = {2, 4};
        List<Double> scores_2 = new ArrayList<>();
        List<Double> scores_4 = new ArrayList<>();
        for (int value : fill) {
            for (int i = 0; i < cells.size(); i++) {
                this.grid.insertTitle(cells.get(i)[0], cells.get(i)[1], value);
                if (value == 2) scores_2.add(i, -this.grid.smoothness() + this.grid.islands());
                if (value == 4) scores_4.add(i, -this.grid.smoothness() + this.grid.islands());
                this.grid.removeTile(cells.get(i)[0], cells.get(i)[1]);
            }
        }

        // 找出使格局变得最坏的所有可能操作
        double maxScore = Math.max(Collections.max(scores_2), Collections.max(scores_4));
         for (int value : fill) {
             if (value == 2) {
                 for (Double fitness : scores_2) {
                    if (fitness == maxScore) {
                        int index = scores_2.indexOf(fitness);
                        candidates.add(new Candidate(cells.get(index)[0], cells.get(index)[1], value));
                    }
                 }
            }
            if (value == 4) {
                for (Double fitness : scores_4) {
                    if (fitness == maxScore) {
                        int index = scores_4.indexOf(fitness);
                        candidates.add(new Candidate(cells.get(index)[0], cells.get(index)[1], value));
                    }
                }
            }
        }

        // 然后遍历这些操作，基于这些操作向下搜索，找到使得格局最坏的分支
        for (int i = 0; i < candidates.size(); i++) {
            int pos_x = candidates.get(i).x;
            int pos_y = candidates.get(i).y;
            int value = candidates.get(i).value;
            GameState newGrid = new GameState(this.grid.getCellMatrix());
            // 电脑即对手做出一个可能的对于电脑来说最好的（对于玩家来说最坏的）决策
            newGrid.insertTitle(pos_x, pos_y, value);
            positions++;
            AI newAI = new AI(newGrid);
            // 向下搜索，下一层为Max层，轮到玩家进行决策
            newAI.grid.playerTurn = true;
            // 这里depth没有减1是为了保证搜索到最深的层为Max层
            result = newAI.search(depth, alpha, bestScore, positions, cutoffs);
            positions = result.positions;
            cutoffs = result.cutoffs;

            // 该层为Min层，哪个分支的局势最不好，就选哪个分支，这里的bestScore代表beta
            if (result.score < bestScore) {
                bestScore = result.score;
            }
            // 如果当前bestScore也即beta<alpha时，表明这个节点下不会再有更好解，于是剪枝
            if (bestScore < alpha) {
                cutoffs++;  //减枝
                return new SearchResult(-1, alpha, positions, cutoffs);
            }
        }
    }

    return new SearchResult(bestMove, bestScore, positions, cutoffs);
}
```

##### 4. 己方格局评价函数实现

4.1 平滑性

```java
/**
 * 测量网格的平滑程度(这些块的值可以形象地解释为海拔)。
 * 相邻两个方块的值差异越小，格局就越平滑(在log空间中，所以它表示在合并之前需要进行的合并的数量)。
 *
 * @return
 */
public double smoothness() {
    int smoothness = 0;
    for (int x = 0; x < 4; x++) {
        for (int y = 0; y < 4; y++) {
            if (this.cellMatrix[x][y] != 0) {
                double value = Math.log(this.cellMatrix[x][y]) / Math.log(2);
                // 计算水平方向和垂直方向的平滑性评估值
                for (int direction = 1; direction <= 2; direction++) {
                    int[] vector = this.vectors[direction];
                    int cnt_x = x, cnt_y = y;
                    do {
                        cnt_x += vector[0];
                        cnt_y += vector[1];
                    } while (isInBounds(cnt_x, cnt_y) && isCellAvailable(cnt_x, cnt_y));
                    if (isInBounds(cnt_x, cnt_y)) {
                        if (cellMatrix[cnt_x][cnt_y] != 0) {
                            double targetValue = Math.log(cellMatrix[cnt_x][cnt_y]) / Math.log(2);
                            smoothness -= Math.abs(value - targetValue);
                        }
                    }
                }
            }
        }
    }
    return smoothness;
}
```

4.2 单调性

```java
/**
 * 测量网格的单调性。
 * 这意味着在向左/向右和向上/向下的方向，方块的值都是严格递增或递减的。
 *
 * @return
 */
public double monotonicity() {
    // 保存四个方向格局单调性的评估值
    int[] totals = {0, 0, 0, 0};

    // 左/右 方向
    for (int x = 0; x < 4; x++) {
        int current = 0;
        int next = current + 1;
        while (next < 4) {
            while (next < 4 && this.cellMatrix[x][next] == 0) next++;
            if (next >= 4) next--;
            double currentValue = (this.cellMatrix[x][current] != 0) ? Math.log(this.cellMatrix[x][current]) / Math.log(2) : 0;
            double nextValue = (this.cellMatrix[x][next] != 0) ? Math.log(this.cellMatrix[x][next]) / Math.log(2) : 0;
            if (currentValue > nextValue) {
                totals[0] += nextValue - currentValue;
            } else if (nextValue > currentValue) {
                totals[1] += currentValue - nextValue;
            }
            current = next;
            next++;
        }
    }

    // 上/下 方向
    for (int y = 0; y < 4; y++) {
        int current = 0;
        int next = current + 1;
        while (next < 4) {
            while (next < 4 && this.cellMatrix[next][y] == 0) next++;
            if (next >= 4) next--;
            double currentValue = (this.cellMatrix[current][y] != 0) ? Math.log(this.cellMatrix[current][y]) / Math.log(2) : 0;
            double nextValue = (this.cellMatrix[next][y] != 0) ? Math.log(this.cellMatrix[next][y]) / Math.log(2) : 0;
            if (currentValue > nextValue) {
                totals[2] += nextValue - currentValue;
            } else if (nextValue > currentValue) {
                totals[3] += currentValue - nextValue;
            }
            current = next;
            next++;
        }
    }

    // 取四个方向中最大的值为当前格局单调性的评估值
    return Math.max(totals[0], totals[1]) + Math.max(totals[2], totals[3]);
}
```

4.3 空格数

```java
private int getEmptyNum(int[][] matrix) {
    int sum = 0;
    for (int i = 0; i < matrix.length; i++)
        for (int j = 0; j < matrix[0].length; j++)
            if (matrix[i][j] == 0) sum++;
    return sum;
}
```

4.4 最大数

```java
/**
 * 取最大数，这里取对数是为与前面其它指标的计算保持一致，均在log空间进行
 *
 * @return
 */
public double maxValue() {
    return Math.log(ArrayUtil.getMax(cellMatrix)) / Math.log(2);
}
```

#####5. 对方格局评价函数实现

对方对于格局的目标就是使得连通个数变多并且使得平滑性降低，表现效果就是使得格局趋于散乱，让玩家难以合并相同的数字。

```java
/**
 * 递归调用计算当前格局的连通块个数
 * 
 * @return
 */
public int islands() {
    int islands = 0;

    marked = new boolean[4][4];
    for (int x = 0; x < 4; x++) {
        for (int y = 0; y < 4; y++) {
            if (this.cellMatrix[x][y] != 0) {
                this.marked[x][y] = false;
            }
        }
    }
    for (int x = 0; x < 4; x++) {
        for (int y = 0; y < 4; y++) {
            if (this.cellMatrix[x][y] != 0 && !this.marked[x][y]) {
                islands++;
                mark(x, y, this.cellMatrix[x][y]);
            }
        }
    }

    return islands;
}

private void mark(int x, int y, int value) {
    if (x >= 0 && x <= 3 && y >= 0 && y <= 3 && (this.cellMatrix[x][y] != 0)
            && (this.cellMatrix[x][y] == value) && (!this.marked[x][y])) {
        this.marked[x][y] = true;
        for (int direction = 0; direction < 4; direction++) {
            int[] vector = this.vectors[direction];
            mark(x + vector[0], y + vector[1], value);
        }
    }
}
```

##### 6. 迭代深搜

```java
// 执行搜索操作，返回最好的移动方向
public int getBestMove() {
    return this.iterativeDeep(100);
}

// 基于alpha-beta的Minimax搜索，进行迭代深搜，搜索时间设定为0.1秒，即决策的思考时间为0.1秒
private int iterativeDeep(long minSearchTime) {
    long start = new Date().getTime();
    int depth = 0;
    int best = -1;
    do {
        SearchResult newBest = this.search(depth, -10000, 10000, 0, 0);
        if (newBest.move == -1) break;
        else best = newBest.move;
        depth++;
    } while (new Date().getTime() - start < minSearchTime);
    return best;
}
```

####实验结果

自动运行结果

<img src="screenshots\QQ图片20180111223607.jpg" style="zoom:60%" />

<img src="screenshots\Screenshot_2018-01-11-22-03-37-853_com.admin.md20.png" style="zoom:30%" />

<img src="screenshots\Screenshot_2018-01-12-10-58-22-936_com.admin.md20.png" style="zoom:30%" />

<img src="screenshots\Screenshot_2018-01-12-17-40-44-378_com.admin.md20.png" style="zoom:30%" />

<img src="screenshots\Screenshot_2018-01-13-15-10-55-737_com.admin.md20.png" style="zoom:30%" />

<img src="screenshots\Screenshot_2018-01-13-14-47-50-215_com.admin.md20.png" style="zoom:30%" />



#### 结论

游戏AI的决策过程，是标准的Minimax Search 结合 Alpha Beta Pruning的实现。 所有的方向(上下左右)都会去尝试。然而在对手做决策时，不是每个空格都去尝试填2或4。 而是选择了最坏的局面，做为搜索分支的剪枝条件，选择性地丢弃了很多搜索分支。对于选择性忽略搜索节点，在某些情况下，会失去获取最优解的机会。不过砍掉了很多分支后, 其搜索深度大大加强，生存能力更强大。另外，超时判断在每个深度探索结束后进行， 这未必会精确，甚至误差很大，但不管如何游戏AI基本达到了每100ms决策一步的要求。最后，根据原作者ovolve创造性的思维和建模，把环境拟人化的对弈模型，使得传统的博弈树模型能应用于此，这是面对反馈类场景的一种很好的评估决策思路。

#### References

https://stackoverflow.com/questions/22342854/what-is-the-optimal-algorithm-for-the-game-2048

http://ov3y.github.io/2048-AI/

https://github.com/ov3y/2048-AI

https://en.wikipedia.org/wiki/Evaluation_function

<\<Artificial Intelligence : A Modern Approach>> (Third Edition)  第5章 对抗搜索 

http://www.flyingmachinestudios.com/programming/minimax/

https://www.neverstopbuilding.com/blog/2013/12/13/tic-tac-toe-understanding-the-minimax-algorithm13/

http://web.cs.ucla.edu/~rosen/161/notes/alphabeta.html

https://www.cnblogs.com/mumuxinfei/p/4415352.html

http://blog.codinglabs.org/articles/2048-ai-analysis.html

http://www.cnblogs.com/mumuxinfei/p/4305981.html

http://www.cnblogs.com/mumuxinfei/p/4379595.html

http://www.cnblogs.com/mumuxinfei/p/4396339.html

http://www.cnblogs.com/mumuxinfei/p/4398680.html

