// PAT / GPLT L1-002 打印沙漏
// 给定 N 个符号，打印能用掉最多符号的沙漏，最后输出剩余符号数。
//
// 沙漏由从大到小再从小到大的奇数行组成：
//   行宽（符号数）为 2k-1, 2k-3, ..., 1, ..., 2k-3, 2k-1
//   共 2k-1 行，符号总数 = 2 * (1 + 3 + ... + (2k-1)) - 1 = 2*k^2 - 1
// 因此只需找到满足 2*k^2 - 1 <= N 的最大 k。

#include <bits/stdc++.h>
using namespace std;

int main() {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    long long n;
    char c;
    if (!(cin >> n >> c)) return 0;

    // 找最大的 k，使 2*k^2 - 1 <= n
    int k = 0;
    while (2LL * (k + 1) * (k + 1) - 1 <= n) ++k;

    long long used = 2LL * k * k - 1;

    // 上半部分（含中间一个符号）：宽度 2k-1 递减到 1
    for (int i = k; i >= 1; --i) {
        int width = 2 * i - 1;
        int indent = k - i;
        cout << string(indent, ' ') << string(width, c) << '\n';
    }
    // 下半部分：宽度 3 递增到 2k-1
    for (int i = 2; i <= k; ++i) {
        int width = 2 * i - 1;
        int indent = k - i;
        cout << string(indent, ' ') << string(width, c) << '\n';
    }

    cout << n - used << '\n';
    return 0;
}
