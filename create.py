#!/usr/bin/env python3
# coding:utf-8

import sys
import random
import sqlite3

DB_PATH = 'db/database.db'


def users(num):
    sql = '''
          INSERT INTO users VALUES (?,?,?)
          '''.strip()
    rows = []
    for i in range(0, num):
        level = random.randint(1, 20)  # 1 <= level <= 20
        job = random.randint(0, 2)    # job -> (0, 1, 2)
        rows.append((i, level, job))
    print("name, level, job")
    print(rows)
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    try:
        c.executemany(sql, rows)
        conn.commit()
    except Exception as e:
        print(e)
        print("exception on insert into users table.")
    finally:
        conn.close()
    return rows


def gen_data():
    level = random.randint(1, 20)
    total = level * 4
    v = [0, 0, 0, 0]  # strength, dexterity, intelligence, vitality
    for j in range(0, len(v)):
        v[j] = random.randint(0, total)
        total = total - v[j]
    w = [level] + v
    return w


def items(num):
    sql = '''
          INSERT INTO items VALUES (?,?,?,?,?,?)
          '''.strip()
    data = []  # list of list
    rows = []  # list of tuple
    for i in range(0, num):
        d = gen_data()
        while d in data:
            d = gen_data()
        data.append(d)
        r = [i] + d   # name, level, strength, dexterity, intelligence, vitality
        rows.append(tuple(r))
    print("name, level, strength, dexterity, intelligence, vitality")
    print(rows)
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    try:
        c.executemany(sql, rows)
        conn.commit()
    except Exception as e:
        print(e)
        print("exception on insert into items table.")
    finally:
        conn.close()
    return rows


def gen_rel(u_rows, i_rows):
    u = random.randint(0, len(u_rows) - 1)
    i = random.randint(0, len(i_rows) - 1)
    while True:
        # assure user level >= item level
        if u_rows[u][1] >= i_rows[i][1]:
            break
        u = random.randint(0, len(u_rows) - 1)
        i = random.randint(0, len(i_rows) - 1)
    return [u, i]


def cross(u, i, total):
    # A user can have maximum 5 items.
    # A item level should be same or lower than owner level
    print("truncat all the tables: users, items, useritems")
    truncate("users")
    truncate("items")
    truncate("useritems")
    print("re-generate all the tables: users, items, useritems")
    u_rows = users(u)
    i_rows = items(i)

    # real work begins
    sql = '''
          INSERT INTO useritems VALUES (?,?,?)
          '''.strip()
    data = []  # list of list
    rows = []
    for i in range(0, total):
        d = gen_rel(u_rows, i_rows)
        while len(list(filter(lambda t: t[0] == d[0], data))) == 5 or d in data:
            d = gen_rel(u_rows, i_rows)
        data.append(d)
        r = [i] + d  # rel, uname, iname
        rows.append(tuple(r))
    print("rel, user_name, item_name")
    print(rows)
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    try:
        c.executemany(sql, rows)
        conn.commit()
    except Exception as e:
        print(e)
        print("exception on insert into useritems table.")
    finally:
        conn.close()


def truncate(tablename):
    sql = "delete from " + tablename
    print("execute the sql cmd: " + sql)
    conn = sqlite3.connect(DB_PATH)
    c = conn.cursor()
    try:
        c.execute(sql)
        conn.commit()
    except Exception as e:
        print(e)
        print("exception on delete table " + tablename)
    finally:
        conn.close()


def main():
    if len(sys.argv) != 3 and len(sys.argv) != 5:
        print("usage: ./create.py [users|items] number")
        print("usage: ./create.py useritems user_number item_number user_item_number")
        print("usage: ./create.py truncate [users|items|useritems]")
        print("example: ./create.py users 3")
        print("example: ./create.py items 7")
        print("example: ./create.py useritems 5 7 10")
        print("example: ./create.py truncate users")
        print("example: ./create.py truncate items")
        print("example: ./create.py truncate useritems")
        sys.exit(1)

    if sys.argv[1] == "users":
        num = int(sys.argv[2])
        users(num)
    if sys.argv[1] == "items":
        num = int(sys.argv[2])
        items(num)
    if sys.argv[1] == "useritems":
        u_num = int(sys.argv[2])
        i_num = int(sys.argv[3])
        num = int(sys.argv[4])
        cross(u_num, i_num, num)
    if sys.argv[1] == "truncate":
        truncate(sys.argv[2])

if __name__ == '__main__':
    main()
