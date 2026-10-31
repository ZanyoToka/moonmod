/* PAT / GPLT L1-002 打印沙漏 —— 初学者版（C 语言）
 *
 * 思路：不用任何公式，像搭积木一样一层一层往上加。
 *   沙漏最中间是 1 个符号；
 *   往外加"一层"，就是上面加一行、下面加一行，这两行一样宽。
 *   第 k 层（最中间算第 1 层）宽 2k-1，所以再多一层要 2*(2(k+1)-1) = 4k+2 个符号。
 * 加到装不下为止，剩下的就是多出来的符号。
 *
 * 编译：gcc -std=c11 -O2 -Wall -Wextra -o main_simple main_simple.c
 */

#include <stdio.h>

int main(void)
{
    int n;        /* 一共有多少个符号 */
    char c;       /* 用什么符号画 */

    scanf("%d %c", &n, &c);   /* "%c" 前面有空格，会自动跳过输入里的空格 */

    int k = 1;        /* 层数：最中间的 1 个符号算第 1 层 */
    int used = 1;     /* 已经用掉的符号数 */

    /* 试着再多加一层，装不下就停 */
    while (1) {
        int next = 4 * k + 2;      /* 多加一层需要：上下各一行，共 2*(2(k+1)-1) 个 */
        if (used + next > n) {
            break;
        }
        used = used + next;
        k = k + 1;
    }

    /* 打印上半部分（含中间那行）：第 k 层到第 1 层 */
    for (int level = k; level >= 1; level = level - 1) {
        int width = 2 * level - 1;      /* 这一行有几个符号 */
        int space = k - level;          /* 这一行前面补几个空格，才能居中 */

        for (int i = 0; i < space; i = i + 1) {
            printf(" ");
        }
        for (int i = 0; i < width; i = i + 1) {
            printf("%c", c);
        }
        printf("\n");                   /* 这一行结束，换行 */
    }

    /* 打印下半部分：第 2 层到第 k 层（1 那一行上面已经打过了） */
    for (int level = 2; level <= k; level = level + 1) {
        int width = 2 * level - 1;
        int space = k - level;

        for (int i = 0; i < space; i = i + 1) {
            printf(" ");
        }
        for (int i = 0; i < width; i = i + 1) {
            printf("%c", c);
        }
        printf("\n");
    }

    /* 剩下的符号数 */
    printf("%d\n", n - used);

    return 0;
}
