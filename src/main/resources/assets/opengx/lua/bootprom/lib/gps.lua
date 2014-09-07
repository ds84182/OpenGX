--raw metal gps library--

local vector = {}
local _vector = {
	add = function( self, o )
		return vector.new(
			self.x + o.x,
			self.y + o.y,
			self.z + o.z
		)
	end,
	sub = function( self, o )
		return vector.new(
			self.x - o.x,
			self.y - o.y,
			self.z - o.z
		)
	end,
	mul = function( self, m )
		return vector.new(
			self.x * m,
			self.y * m,
			self.z * m
		)
	end,
	dot = function( self, o )
		return self.x*o.x + self.y*o.y + self.z*o.z
	end,
	cross = function( self, o )
		return vector.new(
			self.y*o.z - self.z*o.y,
			self.z*o.x - self.x*o.z,
			self.x*o.y - self.y*o.x
		)
	end,
	length = function( self )
		return math.sqrt( self.x*self.x + self.y*self.y + self.z*self.z )
	end,
	normalize = function( self )
		return self:mul( 1 / self:length() )
	end,
	round = function( self, nTolerance )
	   --[[ nTolerance = nTolerance or 1.0
		return vector.new(
			math.floor( (self.x + (nTolerance * 0.5)) / nTolerance ) * nTolerance,
			math.floor( (self.y + (nTolerance * 0.5)) / nTolerance ) * nTolerance,
			math.floor( (self.z + (nTolerance * 0.5)) / nTolerance ) * nTolerance
		)]]
		return self
	end,
	tostring = function( self )
		return self.x..","..self.y..","..self.z
	end,
}

local vmetatable = {
	__index = _vector,
	__add = _vector.add,
	__sub = _vector.sub,
	__mul = _vector.mul,
	__unm = function( v ) return v:mul(-1) end,
	__tostring = _vector.tostring,
}

function vector.new( x, y, z )
	local v = {
		x = x or 0,
		y = y or 0,
		z = z or 0
	}
	setmetatable( v, vmetatable )
	return v
end

PORT_GPS = 8192

local gps = {}

local function trilaterate( A, B, C )
	if not (A and B and C) then return nil end
	local a2b = B.position - A.position
	local a2c = C.position - A.position
		
	if math.abs( a2b:normalize():dot( a2c:normalize() ) ) > 0.999 then
		return nil
	end
	
	local d = a2b:length()
	local ex = a2b:normalize( )
	local i = ex:dot( a2c )
	local ey = (a2c - (ex * i)):normalize()
	local j = ey:dot( a2c )
	local ez = ex:cross( ey )

	local r1 = A.distance
	local r2 = B.distance
	local r3 = C.distance
		
	local x = (r1*r1 - r2*r2 + d*d) / (2*d)
	local y = (r1*r1 - r3*r3 - x*x + (x-i)*(x-i) + j*j) / (2*j)
		
	local result = A.position + (ex * x) + (ex * y)

	local zSquared = r1*r1 - x*x - y*y
	if zSquared > 0 then
		local z = math.sqrt( zSquared )
		local result1 = result + (ez * z)
		local result2 = result - (ez * z)
		
		local rounded1, rounded2 = result1:round( 0.01 ), result2:round( 0.01 )
		if rounded1.x ~= rounded2.x or rounded1.y ~= rounded2.y or rounded1.z ~= rounded2.z then
			return rounded1, rounded2
		else
			return rounded1
		end
	end
	return result--:round( 0.01 )
end

local function narrow( p1, p2, fix )
	local dist1 = math.abs( (p1 - fix.vPosition):length() - fix.nDistance )
	local dist2 = math.abs( (p2 - fix.vPosition):length() - fix.nDistance )
	
	if math.abs(dist1 - dist2) < 0.01 then
		return p1, p2
	elseif dist1 < dist2 then
		return p1:round( 0.01 )
	else
		return p2:round( 0.01 )
	end
end

function gps.locate( timeout, modem, debug )

	timeout = timeout or 2
	debug = debug and print ~= nil

	if modem == nil then
		if debug then
			print( "No wireless modem attached" )
		end
		return nil
	end
	
	if debug then
		print( "Finding position..." )
	end
	
	-- Open a port
	local port = math.random(1,PORT_GPS-1)
	local openedPort = false
	if not modem.isOpen( port ) then
		modem.open( port )
		openedPort = true
	end
	
	-- Send a ping to listening GPS hosts
	modem.broadcast( PORT_GPS, "GPS", port, "PING" )
		
	-- Wait for the responses
	local fixes = {}
	local pos1, pos2 = nil, nil
	local start = computer.uptime()
	local finish = start+timeout
	while computer.uptime()<=finish do
		local e = {computer.pullSignal(finish-computer.uptime())}
		if e[1] == "modem_message" then
			-- We received a message from a modem
			local address, from, port, distance, header = table.unpack(e,2,6)
			local message = {table.unpack(e,7,#e)}
			if address == modem.address and port == port and header == "GPS" then
				-- Received the correct message from the correct modem: use it to determine position
				if #message == 3 then
					local fix = { position = vector.new( message[1], message[2], message[3] ), distance = distance }
					if debug then
						print( fix.distance.." meters from "..fix.position.x..", "..fix.position.y..", "..fix.position.z )
					end
					if fix.distance == 0 then
					    pos1, pos2 = fix.position, nil
					else
                        table.insert( fixes, fix )
                        if #fixes >= 3 then
                            if not pos1 then
                                pos1, pos2 = trilaterate( fixes[1], fixes[2], fixes[#fixes] )
                            else
                                pos1, pos2 = narrow( pos1, pos2, fixes[#fixes] )
                            end
                        end
                    end
					if pos1 and not pos2 then
						break
					end
				end
			end
		end 
	end
	
	-- Close the port, if we opened one
	if openedPort then
		modem.close( port )
	end
	
	-- Return the response
	if pos1 and pos2 then
		if debug then
			print( "Ambiguous position" )
			print( "Could be "..pos1.x..","..pos1.y..","..pos1.z.." or "..pos2.x..","..pos2.y..","..pos2.z )
		end
		return nil
	elseif pos1 then
		if debug then
			print( "Position is "..pos1.x..","..pos1.y..","..pos1.z )
		end
		return pos1.x, pos1.y, pos1.z
	else
		if debug then
			print( "Could not determine position" )
		end
		return nil
	end
end

return gps

