/* PAT / GPLT L1-002 打印沙漏 —— C 语言版
 *
 * 沙漏每行宽（符号数）为 2k-1, 2k-3, ..., 1, ..., 2k-3, 2k-1，
 * 共 2k-1 行，符号总数 = 2*(1+3+...+(2k-1)) - 1 = 2*k^2 - 1。
 * 因此找到满足 2*k^2 - 1 <= N 的最大 k，剩余 N - (2k^2-1) 个符号。
 *
 * 编译：gcc -std=c11 -O2 -Wall -Wextra -o main main.c
 * 运行：echo "19 *" | ./main
 */

#include <stdio.h>

int main(void)
{
    int n;
    char c;

    if (scanf("%d %c", &n, &c) != 2)
        return 0;

    /* 找最大的 k，使 2*k^2 - 1 <= n（n <= 1000，k 最大 22） */
    int k = 0;
    while (2 * (k + 1) * (k + 1) - 1 <= n)
        ++k;

    int used = 2 * k * k - 1;

    /* 上半部分（含中间只有一个符号的那一行）：宽度 2k-1 → 1 */
    for (int i = k; i >= 1; --i) {
        int width = 2 * i - 1;
        for (int s = 0; s < k - i; ++s)
            putchar(' ');
        for (int s = 0; s < width; ++s)
            putchar(c);
        putchar('\n');
    }

    /* 下半部分：宽度 3 → 2k-1 */
    for (int i = 2; i <= k; ++i) {
        int width = 2 * i - 1;
        for (int s = 0; s < k - i; ++s)
            putchar(' ');
        for (int s = 0; s < width; ++s)
            putchar(c);
        putchar('\n');
    }

    printf("%d\n", n - used);
    return 0;
}
