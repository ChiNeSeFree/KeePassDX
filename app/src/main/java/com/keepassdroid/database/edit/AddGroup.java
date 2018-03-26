/*
 * Copyright 2017 Brian Pellin, Jeremy Jamet / Kunzisoft.
 *     
 * This file is part of KeePass DX.
 *
 *  KeePass DX is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  KeePass DX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with KeePass DX.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.keepassdroid.database.edit;

import android.content.Context;

import com.keepassdroid.database.Database;
import com.keepassdroid.database.PwDatabase;
import com.keepassdroid.database.PwGroup;

public class AddGroup extends RunnableOnFinish {
	protected Database mDb;
	private String mName;
	private int mIconID;
	private PwGroup mGroup;
	private PwGroup mParent;
	private Context ctx;
	private boolean mDontSave;
	
	public AddGroup(Context ctx, Database db, String name, int iconid,
                    PwGroup parent, AfterAddNodeOnFinish afterAddNode,
                    boolean dontSave) {
		super(afterAddNode);

		mDb = db;
		mName = name;
		mIconID = iconid;
		mParent = parent;
		mDontSave = dontSave;
		this.ctx = ctx;

		mFinish = new AfterAdd(mFinish);
	}
	
	@Override
	public void run() {
		PwDatabase pm = mDb.pm;
		
		// Generate new group
		mGroup = pm.createGroup();
		mGroup.initNewGroup(mName, pm.newGroupId());
		mGroup.setIcon(mDb.pm.getIconFactory().getIcon(mIconID));
		pm.addGroupTo(mGroup, mParent);

		// Commit to disk
		SaveDB save = new SaveDB(ctx, mDb, mFinish, mDontSave);
		save.run();
	}
	
	private class AfterAdd extends OnFinish {

		AfterAdd(OnFinish finish) {
			super(finish);
		}

		@Override
		public void run() {
			PwDatabase pm = mDb.pm;
			if ( !mSuccess ) {
                pm.removeGroupFrom(mGroup, mParent);
			}

            // TODO Better callback
            AfterAddNodeOnFinish afterAddNode =
                    (AfterAddNodeOnFinish) super.mOnFinish;
            afterAddNode.mSuccess = mSuccess;
            afterAddNode.mMessage = mMessage;
            afterAddNode.run(mGroup);
		}
	}
}
