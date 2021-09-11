#!/usr/bin/python3

import os
import sqlite3
import argparse

DB_INIT_FILE = "sqlite3.init"
DB_TARGET_PATH = "nat.db"

def clean():
    os.system("rm nat.db")

def createDB():
    os.system("sqlite3 -init {} {} << EOF".format(DB_INIT_FILE, DB_TARGET_PATH))

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Database Generator for Best-Nat-App")
    
    parser.add_argument("--clean", action="store_const", default=False, const=True, help="Remove current database")
    parser.add_argument("--create_db", action="store_const", default=False, const=True, help="(Re-)create Database")

    args = parser.parse_args()
    if args.clean:
        clean()
    if args.create_db:
        createDB()

