# Problem 1. Two Sum (Array, Hash Table)

Given an array of integers nums and an integer target, return indices of the two numbers such that they add up to
target.
You may assume that each input would have exactly one solution, and you may not use the same element twice.
You can return the answer in any order.

Example 1:
Input: nums = [2,7,11,15], target = 9
Output: [0,1]
Explanation: Because nums[0] + nums[1] == 9, we return [0, 1].

Example 2:
Input: nums = [3,2,4], target = 6
Output: [1,2]

Example 3:
Input: nums = [3,3], target = 6
Output: [0,1]

```java
public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> subResults = new HashMap<>(); // (subResult, i)
    // Use Ex2
    for (int i = 0; i < nums.length; i++) {
        int subResult = target - nums[i]; // i = 1 => 6 - 2 = 4
        if (subResults.containsKey(nums[i])) { // i = 2 => has (4, 1)
            return new int[]{subResults.get(nums[i]), i};
        }
        subResults.put(subResult, i); // i = 1 => save (4, 1)
    }
    return new int[]{0, 0};
}
```

# Problem 9. Palindrome Number (Math)

Given an integer x, return true if x is a palindrome, and false otherwise.

Example 1:
Input: x = 121
Output: true
Explanation: 121 reads as 121 from left to right and from right to left.

Example 2:
Input: x = -121
Output: false
Explanation: From left to right, it reads -121. From right to left, it becomes 121-. Therefore it is not a palindrome.

Example 3:
Input: x = 10
Output: false
Explanation: Reads 01 from right to left. Therefore it is not a palindrome.

```java
public boolean isPalindrome(int x) {
    if (x < 0) return false;

    int reverseNum = 0;
    int dividedRs = x; // Ex1: 121

    while (dividedRs > 0) {
        int r = dividedRs % 10; // 121 % 10 = 1 --> 12 % 10 = 2 --> ...
        reverseNum = reverseNum * 10 + r; // 0 * 10 + 1 = 1 --> 1 * 10 + 2 = 12 --> ...
        dividedRs /= 10; // 121 / 10 = 12 --> 12 / 10 = 1 --> ...
    }

    return (x == reverseNum);
}
```

# Problem 13. Roman to Integer (Hash Table, Math, String)

Roman numerals are represented by seven different symbols: I, V, X, L, C, D and M.
Symbol Value
I 1
V 5
X 10
L 50
C 100
D 500
M 1000

For example, 2 is written as II in Roman numeral, just two ones added together.
12 is written as XII, which is simply X + II.
The number 27 is written as XXVII, which is XX + V + II.

Roman numerals are usually written largest to smallest from left to right.
However, the numeral for four is not IIII. Instead, the number four is written as IV.
Because the one is before the five we subtract it making four.
The same principle applies to the number nine, which is written as IX.

There are six instances where subtraction is used:
I can be placed before V (5) and X (10) to make 4 and 9.
X can be placed before L (50) and C (100) to make 40 and 90.
C can be placed before D (500) and M (1000) to make 400 and 900.
Given a roman numeral, convert it to an integer.

Example 1:
Input: s = "III"
Output: 3
Explanation: III = 3.

Example 2:
Input: s = "LVIII"
Output: 58
Explanation: L = 50, V= 5, III = 3.

Example 3:
Input: s = "MCMXCIV"
Output: 1994
Explanation: M = 1000, CM = 900, XC = 90 and IV = 4.

```java
public int romanToInt(String s) {
    Map<Character, Integer> romanDict = new HashMap<>();
    romanDict.put('I', 1);
    romanDict.put('V', 5);
    romanDict.put('X', 10);
    romanDict.put('L', 50);
    romanDict.put('C', 100);
    romanDict.put('D', 500);
    romanDict.put('M', 1000);

    int result = 0;
    for (int i = 0; i < s.length(); i++) {
        int parsedNum = romanDict.get(s.charAt(i));

        if (i < s.length() - 1) {
            // Ex: (I, 1) < (V, 5) => (IV, 5 - 1)
            if (romanDict.get(s.charAt(i)) < romanDict.get(s.charAt(i + 1))) {
                parsedNum *= -1;
            }
        }

        result += parsedNum;
    }

    return result;
}
```

# Problem 14. Longest Common Prefix (Array, String, Trie)

Write a function to find the longest common prefix string amongst an array of strings.
If there is no common prefix, return an empty string "".

Example 1:
Input: strs = ["flower","flow","flight"]
Output: "fl"

Example 2:
Input: strs = ["dog","racecar","car"]
Output: ""
Explanation: There is no common prefix among the input strings.

```java
static class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isEndOfWord;
}

static class Trie {
    TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root; // Begin from root node
        for (char c : word.toCharArray()) {
            node.children.putIfAbsent(c, new TrieNode()); // Create child of current node
            node = node.children.get(c); // Move to current node's child
        }
        node.isEndOfWord = true;
    }

    public String findLongestPrefix() {
        StringBuilder result = new StringBuilder();
        TrieNode node = root; // Begin from root node
        while (node.children.size() == 1 && !node.isEndOfWord) {
            Map.Entry<Character, TrieNode> entry = node.children.entrySet().iterator().next();
            result.append(entry.getKey());
            node = entry.getValue(); // Move to current node's child
        }
        return result.toString();
    }

}

public String longestCommonPrefix(String[] strs) {
    Trie trie = new Trie();
    for (String str : strs) {
        trie.insert(str);
    }
    return trie.findLongestPrefix();
}
```

# Problem 20. Valid Parentheses (String, Stack)

Given a string s containing just the characters '(', ')', '{', '}', '[' and ']',
determine if the input string is valid.

An input string is valid if:
Open brackets must be closed by the same type of brackets.
Open brackets must be closed in the correct order.
Every close bracket has a corresponding open bracket of the same type.

Example 1:
Input: s = "()"
Output: true

Example 2:
Input: s = "()[]{}"
Output: true

Example 3:
Input: s = "(]"
Output: false

Example 4:
Input: s = "([])"
Output: true

Example 5:
Input: s = "([)]"
Output: false

```java
public boolean isValid(String s) {
    if (s.length() % 2 != 0) return false;

    Stack<Character> openBrackets = new Stack<>();
    for (int i = 0; i < s.length(); i++) {
        if (s.charAt(i) == '(' || s.charAt(i) == '[' || s.charAt(i) == '{') {
            openBrackets.push(s.charAt(i));

        } else {
            if (openBrackets.size() > 0) {
                char openBracket = openBrackets.peek();
                if (s.charAt(i) - openBracket == 1 || s.charAt(i) - openBracket == 2) openBrackets.pop();
                else return false;

            } else return false;
        }
    }

    return openBrackets.size() == 0;
}
```

# Problem 21. Merge Two Sorted Lists (Linked List, Recursion)

You are given the heads of two sorted linked lists list1 and list2.
Merge the two lists into one sorted list.
The list should be made by splicing together the nodes of the first two lists.
Return the head of the merged linked list.

Example 1:
Input: list1 = [1,2,4], list2 = [1,3,4]
Output: [1,1,2,3,4,4]

Example 2:
Input: list1 = [], list2 = []
Output: []

Example 3:
Input: list1 = [], list2 = [0]
Output: [0]

```java
public ListNode mergeTwoLists(ListNode list1, ListNode list2) {
    if (list1 == null) return list2;
    if (list2 == null) return list1;

    // 1 -> merge([2,4], [1,3,4])
    if (list1.val <= list2.val) {
        list1.next = mergeTwoLists(list1.next, list2);
        return list1;
    
    // 1 -> 1 -> merge([2,4], [3,4])
    } else {
        list2.next = mergeTwoLists(list1, list2.next);
        return list2;
    }
    
    // 1 -> 1 -> 2 -> merge([4], [3,4])
    // 1 -> 1 -> 2 -> 3 -> merge([4], [4])
    // ... -> 1 1 2 3 4 4
}
```

# Problem 26. Remove Duplicates from Sorted Array (Array, Two Pointers)

Given an integer array nums sorted in non-decreasing order,
remove the duplicates in-place such that each unique element appears only once.
The relative order of the elements should be kept the same. Then return the number of unique elements in nums.

Consider the number of unique elements of nums to be k, to get accepted, you need to do the following things:
Change the array nums such that the first k elements of nums contain the unique elements in the order they were present in nums initially. 
The remaining elements of nums are not important as well as the size of nums.
Return k.

Custom Judge:
The judge will test your solution with the following code:

```
int[] nums = [...]; // Input array
int[] expectedNums = [...]; // The expected answer with correct length

int k = removeDuplicates(nums); // Calls your implementation

assert k == expectedNums.length;
for (int i = 0; i < k; i++) {
    assert nums[i] == expectedNums[i];
}
```
If all assertions pass, then your solution will be accepted.

Example 1:
Input: nums = [1,1,2]
Output: 2, nums = [1,2,_]
Explanation: Your function should return k = 2, with the first two elements of nums being 1 and 2 respectively.
It does not matter what you leave beyond the returned k (hence they are underscores).

Example 2:
Input: nums = [0,0,1,1,1,2,2,3,3,4]
Output: 5, nums = [0,1,2,3,4,_,_,_,_,_]
Explanation: Your function should return k = 5, with the first five elements of nums being 0, 1, 2, 3, and 4
respectively.
It does not matter what you leave beyond the returned k (hence they are underscores).
```java
    public int removeDuplicates(int[] nums) {
    int a = 0;
    // a b
    // 0 1 2 
    // 1 1 2
    for (int b = 1; b < nums.length; b++) {
        if (nums[b] > nums[a]) {
            nums[++a] = nums[b];
        }
    }
    return a + 1;
}
```

# Problem 27. Remove Element (Array, Two Pointers)

Given an integer array nums and an integer val, 
remove all occurrences of val in nums in-place. 
The order of the elements may be changed. Then return the number of elements in nums which are not equal to val.

Consider the number of elements in nums which are not equal to val be k, 
to get accepted, you need to do the following things:
Change the array nums such that the first k elements of nums contain the elements which are not equal to val.
The remaining elements of nums are not important as well as the size of nums.
Return k.

Custom Judge:
The judge will test your solution with the following code:
```
int[] nums = [...]; // Input array
int val = ...; // Value to remove
int[] expectedNums = [...]; // The expected answer with correct length.
// It is sorted with no values equaling val.

int k = removeElement(nums, val); // Calls your implementation

assert k == expectedNums.length;
sort(nums, 0, k); // Sort the first k elements of nums
for (int i = 0; i < actualLength; i++) {
    assert nums[i] == expectedNums[i];
}
```
If all assertions pass, then your solution will be accepted.

Example 1:
Input: nums = [3,2,2,3], val = 3
Output: 2, nums = [2,2,_,_]
Explanation: Your function should return k = 2, with the first two elements of nums being 2.
It does not matter what you leave beyond the returned k (hence they are underscores).

Example 2:
Input: nums = [0,1,2,2,3,0,4,2], val = 2
Output: 5, nums = [0,1,4,0,3,_,_,_]
Explanation: Your function should return k = 5, with the first five elements of nums containing 0, 0, 1, 3, and 4.
Note that the five elements can be returned in any order.
It does not matter what you leave beyond the returned k (hence they are underscores).
```java
public int removeElement(int[] nums, int val) {
    int a = 0;
    // a
    // b
    // 0 1 2 3 
    // 3 2 2 3
    for (int b = 0; b < nums.length; b++) {
        if (nums[b] != val) {
            nums[a++] = nums[b];
        }
    }
    return a;
}
```

# Problem 28. Find the Index of the First Occurrence in a String (Two Pointers, String, String Matching)

Given two strings needle and haystack,
return the index of the first occurrence of needle in haystack,
or -1 if needle is not part of haystack.

Example 1:
Input: haystack = "sadbutsad", needle = "sad"
Output: 0
Explanation: "sad" occurs at index 0 and 6.
The first occurrence is at index 0, so we return 0.

Example 2:
Input: haystack = "leetcode", needle = "leeto"
Output: -1
Explanation: "leeto" did not occur in "leetcode", so we return -1.
```java
public int strStr(String haystack, String needle) {
    if (haystack.length() == needle.length()) return needle.equals(haystack) ? 0 : -1;

    // 0 -> 9 - 3 = 6
    // 0 1 2 3
    // s a d b . . .
    // i = 0 -> begin = 0, end = 3 -> "sad"
    for (int i = 0; i < haystack.length() - needle.length() + 1; i++) {
        if (needle.equals(haystack.substring(i, i + needle.length()))) {
            return i;
        }
    }
    return -1;
}
```
