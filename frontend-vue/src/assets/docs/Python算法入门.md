算法是编程的核心，掌握常见算法不仅能提升代码效率，更能培养解决问题的逻辑思维。Python 作为一门简洁易读的语言，非常适合初学者学习和实现算法。本文将从基础算法概念出发，结合 Python 代码示例，讲解排序、查找、递归、动态规划等经典算法，帮助你快速入门算法世界。

一、算法基础：概念与复杂度分析
1. 什么是算法？
算法是解决问题的**步骤集合**，具有以下特性：

输入：有零个或多个输入；

输出：至少有一个输出；

确定性：每一步都有明确的定义，无歧义；

有限性：在有限步骤内结束；

有效性：每一步都能在有限时间内完成。

2. 时间复杂度与空间复杂度
（1）时间复杂度
描述算法执行时间随输入规模增长的变化趋势，用大 O 符号表示（忽略常数项和低阶项）。常见时间复杂度排序：

O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(n³) < O(2ⁿ) < O(n!)

O(1)：常数时间，如数组索引访问；

O(log n)：对数时间，如二分查找；

O(n)：线性时间，如线性查找；

O(n log n)：线性对数时间，如快速排序、归并排序；

O(n²)：平方时间，如冒泡排序、插入排序。

（2）空间复杂度
描述算法所需额外空间随输入规模增长的变化趋势，同样用大 O 符号表示。

二、基础算法：排序与查找
1. 排序算法
排序算法是最基础的算法之一，用于将一组数据按特定顺序排列。

（1）冒泡排序（Bubble Sort）
核心思想：重复遍历要排序的列表，比较相邻元素，若顺序错误则交换，直到列表有序。

时间复杂度：O(n²)（最坏情况），O(n)（最好情况，已排序）

空间复杂度：O(1)

def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        # 标记是否发生交换，若未交换则列表已有序，提前退出
        swapped = False
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]  # 交换
                swapped = True
        if not swapped:
            break
    return arr

# 测试
arr = [64, 34, 25, 12, 22, 11, 90]
print("冒泡排序结果：", bubble_sort(arr))  # 输出：[11, 12, 22, 25, 34, 64, 90]
AI写代码
python
运行

（2）选择排序（Selection Sort）
核心思想：每次从未排序部分找到最小元素，放到已排序部分的末尾。

时间复杂度：O(n²)（无论输入如何，均需遍历）

空间复杂度：O(1)

def selection_sort(arr):
    n = len(arr)
    for i in range(n):
        # 找到未排序部分的最小元素索引
        min_idx = i
        for j in range(i + 1, n):
            if arr[j] < arr[min_idx]:
                min_idx = j
        # 将最小元素与未排序部分的第一个元素交换
        arr[i], arr[min_idx] = arr[min_idx], arr[i]
    return arr

# 测试
arr = [64, 34, 25, 12, 22, 11, 90]
print("选择排序结果：", selection_sort(arr))  # 输出：[11, 12, 22, 25, 34, 64, 90]
AI写代码
python
运行

（3）插入排序（Insertion Sort）
核心思想：将未排序元素逐个插入到已排序部分的正确位置。

时间复杂度：O(n²)（最坏情况），O(n)（最好情况，已排序）

空间复杂度：O(1)

def insertion_sort(arr):
    n = len(arr)
    for i in range(1, n):
        key = arr[i]  # 当前要插入的元素
        j = i - 1
        # 将大于 key 的元素向右移动
        while j >= 0 and arr[j] > key:
            arr[j + 1] = arr[j]
            j -= 1
        arr[j + 1] = key  # 插入 key 到正确位置
    return arr

# 测试
arr = [64, 34, 25, 12, 22, 11, 90]
print("插入排序结果：", insertion_sort(arr))  # 输出：[11, 12, 22, 25, 34, 64, 90]
AI写代码
python
运行

（4）快速排序（Quick Sort）
核心思想：选择一个基准元素，将列表分为小于基准和大于基准的两部分，递归排序两部分。

时间复杂度：O(n log n)（平均情况），O(n²)（最坏情况，如已排序）

空间复杂度：O(log n)（递归调用栈）

def quick_sort(arr):
    if len(arr) <= 1:
        return arr
    # 选择基准元素（这里选择中间元素）
    pivot = arr[len(arr) // 2]
    # 分为小于、等于、大于基准的三部分
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    # 递归排序左右两部分，合并结果
    return quick_sort(left) + middle + quick_sort(right)

# 测试
arr = [64, 34, 25, 12, 22, 11, 90]
print("快速排序结果：", quick_sort(arr))  # 输出：[11, 12, 22, 25, 34, 64, 90]
AI写代码
python
运行

2. 查找算法
查找算法用于在数据结构 中查找特定元素。

（1）线性查找（Linear Search）
核心思想：从列表开头逐个遍历，直到找到目标元素或遍历结束。

时间复杂度：O(n)

空间复杂度：O(1)

def linear_search(arr, target):
    for i in range(len(arr)):
        if arr[i] == target:
            return i  # 返回目标元素的索引
    return -1  # 未找到返回 -1

# 测试
arr = [64, 34, 25, 12, 22, 11, 90]
target = 25
print(f"线性查找 {target} 的索引：", linear_search(arr, target))  # 输出：2
AI写代码
python
运行

（2）二分查找（Binary Search）
核心思想：仅适用于**有序列表**，每次将查找范围缩小一半，直到找到目标元素或范围为空。

时间复杂度：O(log n)

空间复杂度：O(1)（迭代实现）或 O(log n)（递归实现）

# 迭代实现
def binary_search(arr, target):
    left, right = 0, len(arr) - 1
    while left <= right:
        mid = (left + right) // 2  # 计算中间索引
        if arr[mid] == target:
            return mid  # 找到目标元素
        elif arr[mid] < target:
            left = mid + 1  # 目标在右半部分
        else:
            right = mid - 1  # 目标在左半部分
    return -1  # 未找到

# 测试（注意：二分查找要求列表有序）
arr = [11, 12, 22, 25, 34, 64, 90]
target = 25
print(f"二分查找 {target} 的索引：", binary_search(arr, target))  # 输出：3
AI写代码
python
运行

三、递归算法
递归是指函数调用 自身的编程技巧，常用于解决可分解为相似子问题的问题。

1. 递归的基本条件
终止条件：递归必须有一个明确的终止条件，否则会无限递归；

递归调用：函数调用自身，解决规模更小的子问题；

问题分解：原问题可分解为结构相同的子问题。

2. 经典递归案例
（1）阶乘计算
问题：计算 n!（n 的阶乘，n! = n × (n-1) × ... × 1）

递归公式：

当 n = 0 或 n = 1 时，n! = 1；

当 n > 1 时，n! = n × (n-1)!

def factorial(n):
    if n == 0 or n == 1:
        return 1  # 终止条件
    return n * factorial(n - 1)  # 递归调用

# 测试
print("5! =", factorial(5))  # 输出：120
AI写代码
python
运行
（2）斐波那契数列
问题：计算斐波那契数列的第 n 项（F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2)）

递归公式：

F(0) = 0；

F(1) = 1；

当 n > 1 时，F(n) = F(n-1) + F(n-2)

def fibonacci(n):
    if n == 0:
        return 0  # 终止条件 1
    elif n == 1:
        return 1  # 终止条件 2
    return fibonacci(n - 1) + fibonacci(n - 2)  # 递归调用

# 测试
print("斐波那契数列第 10 项：", fibonacci(10))  # 输出：55
AI写代码
python
运行
注意：递归实现的斐波那契数列存在大量重复计算（如计算 F(5) 时需重复计算 F(3)、F(2) 等），时间复杂度为 O(2ⁿ)。可通过**记忆 化搜索**或**动态规划**优化，时间复杂度降至 O(n)。

四、动态规划算法
动态规划（Dynamic Programming，DP）是一种通过将原问题分解为子问题并存储子问题解，避免重复计算的算法思想。

1. 动态规划的核心思想
最优子结构：原问题的最优解包含子问题的最优解；

重叠子问题：子问题被多次重复计算；

状态转移方程：描述问题状态之间的转移关系；

备忘录：存储子问题的解，避免重复计算。

2. 经典动态规划案例
（1）爬楼梯问题
问题：有 n 级台阶，每次可以爬 1 级或 2 级，共有多少种不同的爬法？

分析：

设 dp[i] 表示爬到第 i 级台阶的不同爬法数；

爬到第 i 级台阶的最后一步只能是从第 i-1 级爬 1 级，或从第 i-2 级爬 2 级；

因此，状态转移方程为：dp[i] = dp[i-1] + dp[i-2]；

初始条件：dp[0] = 1（爬到第 0 级台阶只有 1 种方法：不爬），dp[1] = 1（爬到第 1 级台阶只有 1 种方法：爬 1 级）。

def climb_stairs(n):
    if n == 0 or n == 1:
        return 1
    # 初始化 dp 数组
    dp = [0] * (n + 1)
    dp[0] = 1
    dp[1] = 1
    # 填充 dp 数组
    for i in range(2, n + 1):
        dp[i] = dp[i - 1] + dp[i - 2]
    return dp[n]

# 测试
print("爬 5 级台阶的方法数：", climb_stairs(5))  # 输出：8
AI写代码
python
运行

空间优化：由于 dp[i] 只依赖于 dp[i-1] 和 dp[i-2]，可使用两个变量代替数组，空间复杂度从 O(n) 降至 O(1)。

def climb_stairs_optimized(n):
    if n == 0 or n == 1:
        return 1
    a, b = 1, 1  # a 表示 dp[i-2]，b 表示 dp[i-1]
    for _ in range(2, n + 1):
        a, b = b, a + b  # 更新 a 和 b
    return b

# 测试
print("爬 5 级台阶的方法数（优化版）：", climb_stairs_optimized(5))  # 输出：8
AI写代码
python
运行

（2）最长公共子序列（LCS）
问题：给定两个字符串  text1 和 text2，返回它们的最长公共子序列的长度。

分析：

设 dp[i][j] 表示 text1 的前 i 个字符和 text2 的前 j 个字符的最长公共子序列长度；

状态转移方程：

若 text1[i-1] == text2[j-1]，则 dp[i][j] = dp[i-1][j-1] + 1；

否则，dp[i][j] = max(dp[i-1][j], dp[i][j-1])；

初始条件：dp[0][j] = 0，dp[i][0] = 0（空字符串与任何字符串的公共子序列长度为 0）。

def longest_common_subsequence(text1, text2):
    m, n = len(text1), len(text2)
    # 初始化 dp 二维数组（m+1 行，n+1 列）
    dp = [[0] * (n + 1) for _ in range(m + 1)]
    # 填充 dp 数组
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if text1[i - 1] == text2[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    return dp[m][n]

# 测试
text1 = "abcde"
text2 = "ace"
print(f"{text1} 和 {text2} 的最长公共子序列长度：", longest_common_subsequence(text1, text2))  # 输出：3
AI写代码
python
运行

五、贪心算法
贪心算法是一种在每一步选择中都采取当前状态下最优（即最有利）的选择，从而希望导致结果是全局最优的算法。

1. 贪心算法的核心思想
局部最优：每一步都选择当前最优解；

全局最优：通过局部最优解的积累，最终得到全局最优解；

适用条件：问题必须具有**贪心选择性质**（全局最优解可通过局部最优解的选择达到）和**最优子结构性质**。

2. 经典贪心算法案例
（1）硬币找零问题
问题：给定不同面额的硬币 coins 和一个总金额 amount，编写一个函数来计算可以凑成总金额所需的最少的硬币个数。如果没有任何一种硬币组合能组成总金额，返回 -1。

分析：

贪心策略：每次选择最大面额的硬币，尽可能多地使用该面额，直到总金额为 0；

注意：贪心算法并非对所有硬币面额都有效（如 coins = [1, 3, 4], amount = 6，贪心算法会选择 4+1+1=3 枚，而最优解是 3+3=2 枚），但对于某些面额（如人民币面额）是有效的。

def coin_change_greedy(coins, amount):
    coins.sort(reverse=True)  # 按面额从大到小排序
    count = 0
    for coin in coins:
        if amount == 0:
            break
        # 计算当前面额最多使用的数量
        use = amount // coin
        count += use
        amount -= use * coin
    return count if amount == 0 else -1

# 测试（适用贪心算法的情况）
coins = [25, 10, 5, 1]
amount = 41
print(f"最少硬币个数：", coin_change_greedy(coins, amount))  # 输出：4（25+10+5+1）

# 测试（不适用贪心算法的情况）
coins = [1, 3, 4]
amount = 6
print(f"贪心算法结果：", coin_change_greedy(coins, amount))  # 输出：3（4+1+1），但最优解是 2（3+3）
AI写代码
python
运行

（2）活动选择问题
问题：给定 n 个活动，每个活动有开始时间 s[i] 和结束时间 f[i]，选择最多的互不重叠的活动。

分析：

贪心策略：每次选择结束时间最早的活动，这样剩余时间最多，可安排更多活动；

步骤：

按结束时间对活动排序；

选择第一个活动；

依次选择后续活动，要求其开始时间大于等于前一个活动的结束时间。

def activity_selection(activities):
    # 按结束时间排序
    activities.sort(key=lambda x: x[1])
    selected = [activities[0]]  # 选择第一个活动
    last_end = activities[0][1]  # 记录最后一个选中活动的结束时间
    # 遍历剩余活动
    for activity in activities[1:]:
        if activity[0] >= last_end:  # 开始时间大于等于前一个活动的结束时间
            selected.append(activity)
            last_end = activity[1]
    return selected

# 测试
activities = [(1, 4), (3, 5), (0, 6), (5, 7), (3, 9), (5, 9), (6, 10), (8, 11), (8, 12), (2, 14), (12, 16)]
selected = activity_selection(activities)
print("最多可选择的活动：", selected)  # 输出：[(1, 4), (5, 7), (8, 11), (12, 16)]
print("活动数量：", len(selected))  # 输出：4
AI写代码
python
运行

六、图算法基础
图是一种重要的数据结构，用于表示元素之间的关系。常见的图算法包括深度优先搜索（DFS）、广度优先搜索（BFS ）、最短路径算法等。

1. 图的表示方法
邻接矩阵：使用二维数组表示顶点之间的连接关系，适合稠密图；

邻接表：使用字典或列表表示每个顶点的邻接顶点，适合稀疏图。

2. 深度优先搜索（DFS）
核心思想：从起始顶点出发，尽可能深地访问图中的顶点，直到无法继续，然后回溯。

实现方式：递归或栈。

# 邻接表表示图
graph = {
    'A': ['B', 'C'],
    'B': ['A', 'D', 'E'],
    'C': ['A', 'F'],
    'D': ['B'],
    'E': ['B', 'F'],
    'F': ['C', 'E']
}

# 递归实现 DFS
def dfs_recursive(graph, start, visited=None):
    if visited is None:
        visited = set()
    visited.add(start)
    print(start, end=' ')
    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs_recursive(graph, neighbor, visited)
    return visited

# 测试
print("DFS 递归遍历结果：")
dfs_recursive(graph, 'A')  # 输出：A B D E F C
AI写代码
python
运行

3. 广度优先搜索（BFS）
核心思想：从起始顶点出发，先访问所有相邻顶点，再依次访问相邻顶点的相邻顶点，逐层扩展。

实现方式：队列。

from collections import deque

# 队列实现 BFS
def bfs(graph, start):
    visited = set()
    queue = deque([start])
    visited.add(start)
    while queue:
        vertex = queue.popleft()
        print(vertex, end=' ')
        for neighbor in graph[vertex]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)
    return visited

# 测试
print("\nBFS 遍历结果：")
bfs(graph, 'A')  # 输出：A B C D E F
AI写代码
python
运行

七、算法学习建议
1. 学习步骤
掌握基础数据结构：数组、链表、栈、队列、树、图等；

学习经典算法：排序、查找、递归、动态规划、贪心、图算法等；

刷题练习：通过 LeetCode、牛客网等平台刷题，巩固算法知识；

分析与优化：学会分析算法的时间复杂度和空间复杂度，尝试优化算法；

总结归纳：将相似算法归类总结，理解其核心思想和适用场景。

2. 刷题技巧
从简单到复杂：先刷简单题，掌握基本思路，再逐步挑战中等题和难题；

分类刷题：按算法类型（如排序、动态规划）或数据结构（如链表、树）分类刷题；

多写多练：同一道题尝试多种解法，比较不同解法的优缺点；

复习回顾：定期复习之前做过的题目，避免遗忘；

看题解：遇到不会的题目，先思考，再参考题解，理解解题思路。

3. 常用 Python 库
bisect：二分查找算法；

heapq：堆（优先队列）实现；

collections：包含 deque（双端队列）、Counter（计数）等实用数据结构；

itertools：提供迭代器相关的工具函数，如排列、组合等。

八、总结
算法是编程的核心，掌握常见算法不仅能提升代码效率，更能培养解决问题的逻辑思维。本文介绍了 Python 中常见的算法，包括：

基础算法：排序（冒泡、选择、插入、快速）和查找（线性、二分）；

递归算法：阶乘、斐波那契数列；

动态规划：爬楼梯、最长公共子序列；

贪心算法：硬币找零、活动选择；

图算法：DFS、BFS。