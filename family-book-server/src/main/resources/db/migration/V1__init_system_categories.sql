-- 初始化系统默认收支分类
-- 收入分类
INSERT INTO t_category (id, name, type, icon, sort, is_system) VALUES
(1, '工资', 1, 'salary', 1, 1),
(2, '奖金', 1, 'bonus', 2, 1),
(3, '投资收益', 1, 'investment', 3, 1),
(4, '兼职', 1, 'part-time', 4, 1),
(5, '红包', 1, 'redpacket', 5, 1),
(6, '其他收入', 1, 'other', 6, 1);

-- 支出分类
INSERT INTO t_category (id, name, type, icon, sort, is_system) VALUES
(101, '餐饮', 2, 'food', 1, 1),
(102, '交通', 2, 'transport', 2, 1),
(103, '购物', 2, 'shopping', 3, 1),
(104, '娱乐', 2, 'entertainment', 4, 1),
(105, '居住', 2, 'housing', 5, 1),
(106, '医疗', 2, 'medical', 6, 1),
(107, '教育', 2, 'education', 7, 1),
(108, '人情', 2, 'gift', 8, 1),
(109, '通讯', 2, 'phone', 9, 1),
(110, '其他支出', 2, 'other', 10, 1);
