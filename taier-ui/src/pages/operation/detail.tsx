import {useContext, useEffect, useMemo, useRef, useState} from 'react';
import {Component} from 'react';
import React from 'react';
import CodeDiff from "react-code-diff-lite";

import { Spin, Layout } from 'antd';
import ReactDOM from 'react-dom';
import molecule from '@dtinsight/molecule';
import type { FormInstance } from 'antd';
import {Button, Checkbox, Divider, message, Space, Tabs} from 'antd';
import type { ColumnsType } from 'antd/lib/table/interface';
import moment from 'moment';
import { history } from 'umi';

import type { IActionRef } from '@/components/sketch';
import Sketch from '@/components/sketch';
import Api from "@/api";
import {DRAWER_MENU_ENUM} from "@/constant";
import context from "@/context";

const WordDiff = () => {

    const [divElement, setDivElement] = useState(<div>这是一个新的 div 元素</div>);
    const [newString,setNewString] = useState("");
    const [oldString,setOldString] = useState("");

    const getRecordDetail = async () => {
        const recordId = history.location.query?.rid
        const recordVersion = history.location.query?.rv
        let rid: number = recordId as unknown as number;
        let rv: number = recordVersion as unknown as number;

        return await Api.getTaskRecordDetail({
            recordId: rid,
            recordVersion: rv,
        }).then((res) => {
            if (res.code === 1) {
                const result = JSON.parse(res.data.sqlText);
                setNewString(result.new_value);
                setOldString(result.old_value);
            }
        });
    };

    useEffect(() => {
        getRecordDetail();


    }, []);



    return (
        <div>
            <CodeDiff
                oldStr={oldString}
                newStr={newString}
                context={100}
                outputFormat="side-by-side"
                theme="light"
            />
        </div>
    )
}

export default WordDiff;
