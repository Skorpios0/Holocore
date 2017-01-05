/***********************************************************************************
* Copyright (c) 2015 /// Project SWG /// www.projectswg.com                        *
*                                                                                  *
* ProjectSWG is the first NGE emulator for Star Wars Galaxies founded on           *
* July 7th, 2011 after SOE announced the official shutdown of Star Wars Galaxies.  *
* Our goal is to create an emulator which will provide a server for players to     *
* continue playing a game similar to the one they used to play. We are basing      *
* it on the final publish of the game prior to end-game events.                    *
*                                                                                  *
* This file is part of Holocore.                                                   *
*                                                                                  *
* -------------------------------------------------------------------------------- *
*                                                                                  *
* Holocore is free software: you can redistribute it and/or modify                 *
* it under the terms of the GNU Affero General Public License as                   *
* published by the Free Software Foundation, either version 3 of the               *
* License, or (at your option) any later version.                                  *
*                                                                                  *
* Holocore is distributed in the hope that it will be useful,                      *
* but WITHOUT ANY WARRANTY; without even the implied warranty of                   *
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                    *
* GNU Affero General Public License for more details.                              *
*                                                                                  *
* You should have received a copy of the GNU Affero General Public License         *
* along with Holocore.  If not, see <http://www.gnu.org/licenses/>.                *
*                                                                                  *
***********************************************************************************/
package resources.objects.building;

import network.packets.swg.zone.baselines.Baseline.BaselineType;
import resources.Location;
import resources.client_info.ClientFactory;
import resources.client_info.visitors.PortalLayoutData;
import resources.client_info.visitors.ObjectData.ObjectDataAttribute;
import resources.control.Assert;
import resources.network.NetBufferStream;
import resources.objects.SWGObject;
import resources.objects.cell.CellObject;
import resources.objects.tangible.TangibleObject;
import resources.server_info.SynchronizedMap;
import services.objects.ObjectCreator;
import intents.object.ObjectCreatedIntent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BuildingObject extends TangibleObject {
	
	private final Map<String, CellObject> nameToCell;
	private final Map<Integer, CellObject> idToCell;
	
	public BuildingObject(long objectId) {
		super(objectId, BaselineType.BUIO);
		nameToCell = new SynchronizedMap<>();
		idToCell = new SynchronizedMap<>();
	}
	
	public CellObject getCellByName(String cellName) {
		return nameToCell.get(cellName);
	}
	
	public CellObject getCellByNumber(int cellNumber) {
		return idToCell.get(cellNumber);
	}
	
	public List<CellObject> getCells() {
		return new ArrayList<>(idToCell.values());
	}
	
	@Override
	public void addObject(SWGObject object) {
		super.addObject(object);
		Assert.test(object instanceof CellObject);
		
		CellObject cell = (CellObject) object;
		Assert.test(cell.getNumber() > 0);
		Assert.test(cell.getNumber() < getCellCount());
		cell.setCellName(getCellName(cell.getNumber()));
		synchronized (idToCell) {
			Assert.isNull(idToCell.get(cell.getNumber()));
			idToCell.put(cell.getNumber(), cell);
			nameToCell.put(cell.getName(), cell); // Can be multiple cells with the same name
		}
	}
	
	public void populateCells() {
		int cells = getCellCount();
		synchronized (idToCell) {
			for (int i = 1; i < cells; i++) { // 0 is world
				if (idToCell.get(i) != null)
					continue;
				CellObject cell = (CellObject) ObjectCreator.createObjectFromTemplate("object/cell/shared_cell.iff");
				Assert.notNull(cell);
				cell.setNumber(i);
				cell.setLocation(new Location(0, 0, 0, getTerrain()));
				addObject(cell);
				new ObjectCreatedIntent(cell).broadcast();
			}
		}
	}
	
	private int getCellCount() {
		PortalLayoutData data = getPortalLayoutData();
		if (data == null)
			return 0;
		return data.getCells().size();
	}
	
	private String getCellName(int cell) {
		PortalLayoutData data = getPortalLayoutData();
		if (data == null)
			return "";
		return data.getCells().get(cell).getName();
	}
	
	private PortalLayoutData getPortalLayoutData() {
		String portalFile = (String) getDataAttribute(ObjectDataAttribute.PORTAL_LAYOUT_FILENAME);
		if (portalFile == null || portalFile.isEmpty())
			return null;
		
		PortalLayoutData portalLayoutData = (PortalLayoutData) ClientFactory.getInfoFromFile(portalFile, true);
		Assert.test(portalLayoutData != null && portalLayoutData.getCells() != null && portalLayoutData.getCells().size() > 0);
		return portalLayoutData;
	}
	
	@Override
	public void save(NetBufferStream stream) {
		super.save(stream);
		stream.addByte(0);
	}
	
	@Override
	public void read(NetBufferStream stream) {
		super.read(stream);
		stream.getByte();
	}
	
}
